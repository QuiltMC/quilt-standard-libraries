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

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import org.quiltmc.qsl.tag.mixin.RequiredTagListRegistryAccessor;

@ApiStatus.Internal
public final class ClientTagRegistryManager<T> {
	private static final Map<RegistryKey<? extends Registry<?>>, ClientTagRegistryManager<?>> TAG_GROUP_MANAGERS =
			new Object2ObjectOpenHashMap<>();

	private final RegistryKey<? extends Registry<T>> registryKey;
	private final TagGroupLoader<T> loader;
	private DynamicRegistryManager registryManager = DynamicRegistryManager.create();
	private Map<Identifier, Tag.Builder> serializedTags = Map.of();
	private TagGroup<T> group;
	private Map<Identifier, Tag.Builder> defaultSerializedTags = Map.of();
	private TagGroup<T> defaultGroup;

	private ClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		this.registryKey = registryKey;
		this.loader = new TagGroupLoader<>(new ClientRegistryFetcher(), dataType);
	}

	public static <T> Tag.Identified<T> create(Identifier id, TagType type,
	                                           RegistryKey<? extends Registry<T>> registryKey,
	                                           Supplier<TagGroup<T>> serverTagGroupSupplier) {
		return switch (type) {
			case CLIENT_SERVER_SYNC -> new ClientTagDelegate<>(id, type, serverTagGroupSupplier,
					getClientTagRegistryManager(registryKey)::getDefaultGroup);
			case CLIENT_ONLY -> new TagDelegate<>(id, type, getClientTagRegistryManager(registryKey)::getGroup);
			default -> throw new IllegalStateException("The client tag registry manager can only create client-sided tags");
		};
	}

	public void setSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.serializedTags = serializedTags;
		this.group = this.loader.buildGroup(this.serializedTags);
	}

	public void setDefaultSerializedTags(Map<Identifier, Tag.Builder> serializedTags) {
		this.defaultSerializedTags = serializedTags;
		this.defaultGroup = this.loader.buildGroup(this.defaultSerializedTags);
	}

	public Map<Identifier, Tag.Builder> load(ResourceManager resourceManager) {
		return this.loader.loadTags(resourceManager);
	}

	public void apply(DynamicRegistryManager registryManager) {
		this.registryManager = registryManager;
		// @TODO force all the tags to be optional if it's a dynamic registry. (Client can't know in advance)
		this.group = this.loader.buildGroup(this.serializedTags);
		this.defaultGroup = this.loader.buildGroup(this.defaultSerializedTags);
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

	public static void applyAll(DynamicRegistryManager registryManager) {
		TAG_GROUP_MANAGERS.forEach((registryKey, manager) -> manager.apply(registryManager));
	}

	/**
	 * Represents a registry content fetcher.
	 * <p>
	 * But this fetcher will fetch the dynamic registry manager and auto-updates its references to the under-laying registry
	 * used for content fetching.
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
