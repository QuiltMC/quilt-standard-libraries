/*
 * Copyright 2021-2022 QuiltMC
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.tag.TagKey;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.mixin.client.TagGroupLoaderAccessor;

@ApiStatus.Internal
public final class ClientTagRegistryManager<T> {
	private static final Map<RegistryKey<? extends Registry<?>>, ClientTagRegistryManager<?>> TAG_GROUP_MANAGERS =
			new WeakHashMap<>();

	private final RegistryKey<? extends Registry<T>> registryKey;
	private final ClientRegistryFetcher registryFetcher;
	private final TagGroupLoader<Holder<T>> loader;
	private DynamicRegistryManager registryManager = BuiltinRegistries.field_36476;
	private Map<Identifier, Tag.Builder> serializedTags = Map.of();
	private Map<TagKey<T>, Tag<Holder<T>>> clientOnlyValues;
	private Map<Identifier, Tag.Builder> fallbackSerializedTags = Map.of();
	private Map<TagKey<T>, Tag<Holder<T>>> fallbackValues;

	private ClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		this.registryKey = registryKey;
		this.registryFetcher = new ClientRegistryFetcher();
		this.loader = new TagGroupLoader<>(this.registryFetcher, dataType);
	}

	public Tag<Holder<T>> getClientTag(TagKey<T> key) {
		if (this.clientOnlyValues != null) {
			return this.clientOnlyValues.getOrDefault(key, Tag.getEmpty());
		}

		return Tag.getEmpty();
	}

	public Stream<TagRegistry.TagEntry<T>> streamClientTags() {
		return this.clientOnlyValues.entrySet().stream().map(entry -> new TagRegistry.TagEntry<>(entry.getKey(), entry.getValue()));
	}

	@SuppressWarnings("unchecked")
	@Environment(EnvType.CLIENT)
	public void setSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.serializedTags = serializedTags;
		this.clientOnlyValues = this.buildDynamicGroup(this.serializedTags, TagType.CLIENT_ONLY);
		this.bindTags(this.clientOnlyValues, (ref, tags) -> ((QuiltRegistryEntryReferenceHooks<T>) ref).quilt$setClientTags(tags));
	}

	public Tag<Holder<T>> getFallbackTag(TagKey<T> key) {
		if (this.fallbackValues != null) {
			return this.fallbackValues.getOrDefault(key, Tag.getEmpty());
		}

		return Tag.getEmpty();
	}

	public Stream<TagRegistry.TagEntry<T>> streamFallbackTags(Predicate<Map.Entry<TagKey<T>, Tag<Holder<T>>>> filter) {
		return this.clientOnlyValues.entrySet().stream()
				.filter(filter)
				.map(entry -> new TagRegistry.TagEntry<>(entry.getKey(), entry.getValue()));
	}

	@SuppressWarnings("unchecked")
	@Environment(EnvType.CLIENT)
	public void setFallbackSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.fallbackSerializedTags = serializedTags;
		this.fallbackValues = this.buildDynamicGroup(this.fallbackSerializedTags, TagType.CLIENT_FALLBACK);
		this.bindTags(this.fallbackValues, (ref, tags) -> ((QuiltRegistryEntryReferenceHooks<T>) ref).quilt$setFallbackTags(tags));
	}

	@Environment(EnvType.CLIENT)
	public Map<Identifier, Tag.Builder> load(ResourceManager resourceManager) {
		return this.loader.loadTags(resourceManager);
	}

	@Environment(EnvType.CLIENT)
	public void apply(DynamicRegistryManager registryManager) {
		this.registryManager = registryManager;

		this.setSerializedTags(this.serializedTags);
		this.setFallbackSerializedTags(this.fallbackSerializedTags);
	}

	@Environment(EnvType.CLIENT)
	private Map<TagKey<T>, Tag<Holder<T>>> buildDynamicGroup(Map<Identifier, Tag.Builder> tagBuilders, TagType type) {
		if (!TagRegistryImpl.isRegistryDynamic(this.registryKey)) {
			var tags = new Object2ObjectOpenHashMap<TagKey<T>, Tag<Holder<T>>>();
			var built = this.loader.buildGroup(tagBuilders);
			built.forEach((id, tag) -> tags.put(QuiltTagKey.create(this.registryKey, id, type), tag));
			return tags;
		}

		var tags = new Object2ObjectOpenHashMap<TagKey<T>, Tag<Holder<T>>>();
		Function<Identifier, Tag<Holder<T>>> tagGetter = id -> tags.get(QuiltTagKey.create(this.registryKey, id, type));
		Function<Identifier, Holder<T>> registryGetter = identifier -> this.registryFetcher.apply(identifier).orElse(null);
		Multimap<Identifier, Identifier> tagEntries = HashMultimap.create();

		tagBuilders.forEach((tagId, builder) -> builder.visitRequiredDependencies(
				tagEntryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, tagEntryId)
		));
		tagBuilders.forEach((tagId, builder) -> builder.visitOptionalDependencies(
				entryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, entryId)
		));

		var set = new HashSet<Identifier>();
		tagBuilders.keySet().forEach(tagId ->
				TagGroupLoaderAccessor.invokeVisitDependenciesAndEntry(tagBuilders, tagEntries, set, tagId,
						(currentTagId, builder) -> tags.put(QuiltTagKey.create(this.registryKey, tagId, type),
								this.buildLenientTag(builder, tagGetter, registryGetter))
				)
		);
		return tags;
	}

	@Environment(EnvType.CLIENT)
	private Tag<Holder<T>> buildLenientTag(Tag.Builder tagBuilder,
	                                       Function<Identifier, Tag<Holder<T>>> tagGetter, Function<Identifier, Holder<T>> objectGetter) {
		ImmutableSet.Builder<Holder<T>> builder = ImmutableSet.builder();

		tagBuilder.streamEntries().forEach(trackedEntry -> trackedEntry.entry().resolve(tagGetter, objectGetter, builder::add));

		return new Tag<>(builder.build());
	}

	@Environment(EnvType.CLIENT)
	public void bindTags(Map<TagKey<T>, Tag<Holder<T>>> map, BiConsumer<Holder.Reference<T>, List<TagKey<T>>> consumer) {
		var registry = this.registryManager.getOptional(this.registryKey);

		if (registry.isEmpty()) {
			return;
		}

		var boundTags = new IdentityHashMap<Holder.Reference<T>, List<TagKey<T>>>();
		registry.get().method_40270().forEach(reference -> boundTags.put(reference, new ArrayList<>()));

		map.forEach((tagKey, tag) -> {
			for (var holder : tag.values()) {
				if (!holder.isRegistry(registry.get())) {
					throw new IllegalStateException(
							"Can't create named set " + tagKey + " containing value "
									+ holder + " from outside registry " + registry.get()
					);
				}

				if (!(holder instanceof Holder.Reference)) {
					throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagKey);
				}

				boundTags.get(holder).add(tagKey);
			}
		});

		boundTags.forEach(consumer);
	}

	@SuppressWarnings("unchecked")
	public static <T> ClientTagRegistryManager<T> get(RegistryKey<? extends Registry<T>> registryKey) {
		return (ClientTagRegistryManager<T>) TAG_GROUP_MANAGERS.computeIfAbsent(registryKey,
				key -> new ClientTagRegistryManager<>(registryKey, TagManagerLoader.getRegistryDirectory(key))
		);
	}

	@Environment(EnvType.CLIENT)
	static void init() {
		Registry.REGISTRIES.forEach(registry -> {
			get(registry.getKey());
		});
		BuiltinRegistries.REGISTRIES.forEach(registry -> {
			get(registry.getKey());
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
	private class ClientRegistryFetcher implements Function<Identifier, Optional<Holder<T>>> {
		private boolean firstCall = true;
		private DynamicRegistryManager lastRegistryManager;
		private Registry<T> cached;

		private ClientRegistryFetcher() {
		}

		@Override
		public Optional<Holder<T>> apply(Identifier id) {
			if (firstCall || ClientTagRegistryManager.this.registryManager != this.lastRegistryManager) {
				this.lastRegistryManager = ClientTagRegistryManager.this.registryManager;
				this.cached = this.lastRegistryManager.getOptional(ClientTagRegistryManager.this.registryKey)
						.orElse(null);
				this.firstCall = false;
			}

			if (this.cached == null) {
				return Optional.empty();
			} else {
				return this.cached.method_40264(RegistryKey.of(this.cached.getKey(), id));
			}
		}
	}
}
