/*
 * Copyright 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.tag.impl.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.TagDelegate;
import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.mixin.RequiredTagListRegistryAccessor;
import org.quiltmc.qsl.tag.mixin.client.TagGroupLoaderAccessor;

@ApiStatus.Internal
public final class ClientTagRegistryManager<T> {
	private static final Map<RegistryKey<? extends Registry<?>>, ClientTagRegistryManager<?>> TAG_GROUP_MANAGERS =
			new Object2ObjectOpenHashMap<>();

	private final RegistryKey<? extends Registry<T>> registryKey;
	private final ClientRegistryFetcher registryFetcher;
	private final TagGroupLoader<T> loader;
	private DynamicRegistryManager registryManager = DynamicRegistryManager.create();
	private Map<Identifier, Tag.Builder> serializedTags = Map.of();
	private TagGroup<T> group;
	private Map<Identifier, Tag.Builder> defaultSerializedTags = Map.of();
	private TagGroup<T> defaultGroup;

	private ClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		this.registryKey = registryKey;
		this.registryFetcher = new ClientRegistryFetcher();
		this.loader = new TagGroupLoader<>(this.registryFetcher, dataType);
	}

	public static <T> Tag.Identified<T> create(Identifier id, TagType type,
	                                           RegistryKey<? extends Registry<T>> registryKey,
	                                           Supplier<TagGroup<T>> serverTagGroupSupplier) {
		return switch (type) {
			case CLIENT_FALLBACK -> new ClientTagDelegate<>(id, type, serverTagGroupSupplier,
					getClientTagRegistryManager(registryKey)::getDefaultGroup);
			case CLIENT_ONLY -> new TagDelegate<>(id, type, getClientTagRegistryManager(registryKey)::getGroup);
			default -> throw new IllegalStateException("The client tag registry manager can only create client-sided tags");
		};
	}

	@Environment(EnvType.CLIENT)
	public void setSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.serializedTags = serializedTags;
		this.group = this.buildDynamicGroup(this.serializedTags);
	}

	@Environment(EnvType.CLIENT)
	public void setDefaultSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.defaultSerializedTags = serializedTags;
		this.defaultGroup = this.buildDynamicGroup(this.defaultSerializedTags);
	}

	@Environment(EnvType.CLIENT)
	public Map<Identifier, Tag.Builder> load(ResourceManager resourceManager) {
		return this.loader.loadTags(resourceManager);
	}

	@Environment(EnvType.CLIENT)
	public void apply(DynamicRegistryManager registryManager) {
		this.registryManager = registryManager;

		this.group = this.buildDynamicGroup(this.serializedTags);
		this.defaultGroup = this.buildDynamicGroup(this.defaultSerializedTags);
	}

	@Environment(EnvType.CLIENT)
	private TagGroup<T> buildDynamicGroup(Map<Identifier, Tag.Builder> tagBuilders) {
		if (!TagRegistryImpl.isRegistryDynamic(this.registryKey)) {
			return this.loader.buildGroup(tagBuilders);
		}

		var tags = new Object2ObjectOpenHashMap<Identifier, Tag<T>>();
		Function<Identifier, Tag<T>> tagGetter = tags::get;
		Function<Identifier, T> registryGetter = identifier -> this.registryFetcher.apply(identifier).orElse(null);
		Multimap<Identifier, Identifier> tagEntries = HashMultimap.create();

		tagBuilders.forEach((tagId, builder) -> builder.forEachTagId(
				tagEntryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, tagEntryId)
		));
		tagBuilders.forEach((tagId, builder) -> builder.forEachGroupId(
				entryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, entryId)
		));

		var set = new HashSet<Identifier>();
		tagBuilders.keySet().forEach(tagId ->
				TagGroupLoaderAccessor.invokeVisitDependenciesAndElement(tagBuilders, tagEntries, set, tagId,
						(currentTagId, builder) -> tags.put(tagId, this.buildLenientTag(builder, tagGetter, registryGetter))
				)
		);
		return TagGroup.create(tags);
	}

	@Environment(EnvType.CLIENT)
	private Tag<T> buildLenientTag(Tag.Builder tagBuilder,
	                               Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter) {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();

		tagBuilder.streamEntries().forEach(trackedEntry -> trackedEntry.getEntry().resolve(tagGetter, objectGetter, builder::add));

		return Tag.of(builder.build());
	}

	public TagGroup<T> getGroup() {
		if (this.group == null) {
			return TagGroup.createEmpty();
		} else {
			return this.group;
		}
	}

	public TagGroup<T> getDefaultGroup() {
		if (this.defaultGroup == null) {
			return TagGroup.createEmpty();
		} else {
			return this.defaultGroup;
		}
	}

	@SuppressWarnings("unchecked")
	static <T> ClientTagRegistryManager<T> getClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey) {
		return (ClientTagRegistryManager<T>) TAG_GROUP_MANAGERS.computeIfAbsent(registryKey, key -> {
			String dataType = null;

			for (var value : RequiredTagListRegistryAccessor.getAll()) {
				if (value.getRegistryKey() == registryKey) {
					dataType = value.getDataType();
					break;
				}
			}

			if (dataType == null) {
				throw new IllegalStateException("Could not find the associated RequiredTagList for " + registryKey + ".");
			}

			return new ClientTagRegistryManager<>(registryKey, dataType);
		});
	}

	static void forEach(Consumer<ClientTagRegistryManager<?>> consumer) {
		TAG_GROUP_MANAGERS.values().forEach(consumer);
	}

	@Environment(EnvType.CLIENT)
	public static void applyAll(DynamicRegistryManager registryManager) {
		TAG_GROUP_MANAGERS.forEach((registryKey, manager) -> manager.apply(registryManager));
	}

	/**
	 * Represents a registry content fetcher.
	 * <p>
	 * This fetcher will auto-update the reference to the underlying registry whenever the dynamic registry manager changes.
	 */
	private class ClientRegistryFetcher implements Function<Identifier, Optional<T>> {
		private boolean firstCall = true;
		private DynamicRegistryManager lastRegistryManager;
		private Registry<T> cached;

		private ClientRegistryFetcher() {
		}

		@Override
		public Optional<T> apply(Identifier id) {
			if (firstCall || ClientTagRegistryManager.this.registryManager != this.lastRegistryManager) {
				this.lastRegistryManager = ClientTagRegistryManager.this.registryManager;
				this.cached = this.lastRegistryManager.getOptional(ClientTagRegistryManager.this.registryKey)
						.orElse(null);
				this.firstCall = false;
			}

			if (this.cached == null) {
				return Optional.empty();
			} else {
				return this.cached.getOrEmpty(id);
			}
		}
	}
}
