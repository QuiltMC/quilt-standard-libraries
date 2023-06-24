/*
 * Copyright 2021 The Quilt Project
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
import java.util.Collection;
import java.util.Collections;
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

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.HolderLookup.RegistryLookup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.mixin.client.DynamicRegistrySyncAccessor;

/**
 * Represents the manager of client-only and fallback tags.
 * <p>
 * This holds the current client tags and the logic to update and re-apply tags to the current in-game context.
 *
 * @param <T> the type of game object the tag holds
 */
@ApiStatus.Internal
public final class ClientTagRegistryManager<T> {
	private static final Map<RegistryKey<? extends Registry<?>>, ClientTagRegistryManager<?>> TAG_GROUP_MANAGERS =
			new WeakHashMap<>();
	private static final HolderLookup.Provider VANILLA_PROVIDERS = DynamicRegistryManager.fromRegistryOfRegistries(Registries.REGISTRY);

	private final RegistryKey<? extends Registry<T>> registryKey;
	/**
	 * Represents a useful object to fetch game objects for the tag group loader.
	 */
	private final RegistryFetcher registryFetcher;
	/**
	 * The Vanilla tag group loader.
	 */
	private final TagGroupLoader<Holder<T>> loader;
	/**
	 * The registry lookup.
	 */
	private HolderLookup.Provider lookupProvider;
	/**
	 * Status of this tag registry manager. Useful state tracking to prevent too early tag application.
	 */
	private ClientRegistryStatus status;
	/**
	 * Represents the serialized client-only tags, stored because the client-only values may change depending on the server's registries.
	 */
	private Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags = Map.of();
	/**
	 * Represents the actual resolved client-only tags, which are only valid for the current in-game context.
	 */
	private Map<TagKey<T>, Collection<Holder<T>>> clientOnlyValues;
	/**
	 * Represents the serialized fallback tags, stored because the fallback values may change depending on the server's registries.
	 */
	private Map<Identifier, List<TagGroupLoader.EntryWithSource>> fallbackSerializedTags = Map.of();
	/**
	 * Represents the actual resolved fallback tags, which are only for the current in-game context.
	 */
	private Map<TagKey<T>, Collection<Holder<T>>> fallbackValues;

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		this.registryKey = registryKey;
		this.lookupProvider = VANILLA_PROVIDERS;

		if (Registries.REGISTRY.contains((RegistryKey) registryKey)) {
			// The registry is static, this means we have only one source of truth that is not updated after starting the game.
			this.registryFetcher = new StaticRegistryFetcher();
			this.status = ClientRegistryStatus.STATIC;
		} else {
			// The registry is dynamic, the values may change throughout the lifecycle of the game.
			this.registryFetcher = new ClientRegistryFetcher();
			this.status = ClientRegistryStatus.WAITING;
		}

		this.loader = new TagGroupLoader<>(this.registryFetcher, dataType);
	}

	public Collection<Holder<T>> getClientTag(TagKey<T> key) {
		if (this.clientOnlyValues != null) {
			return this.clientOnlyValues.getOrDefault(key, Collections.emptySet());
		}

		return Collections.emptyList();
	}

	public Stream<TagRegistry.TagValues<T>> streamClientTags() {
		return this.clientOnlyValues.entrySet().stream().map(entry -> new TagRegistry.TagValues<>(entry.getKey(), entry.getValue()));
	}

	@ClientOnly
	public void setSerializedTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.serializedTags = serializedTags;

		if (this.status.isReady()) {
			// Actually apply if the values are ready to be fetched.
			this.applyTags(this.serializedTags);
		} else {
			this.applyTags(Collections.emptyMap());
		}
	}

	@SuppressWarnings("unchecked")
	@ClientOnly
	private void applyTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.clientOnlyValues = this.buildDynamicGroup(serializedTags, TagType.CLIENT_ONLY);
		this.bindTags(this.clientOnlyValues, (ref, tags) -> ((QuiltHolderReferenceHooks<T>) ref).quilt$setClientTags(tags));
	}

	public Collection<Holder<T>> getFallbackTag(TagKey<T> key) {
		if (this.fallbackValues != null) {
			return this.fallbackValues.getOrDefault(key, Collections.emptySet());
		}

		return Collections.emptySet();
	}

	public Stream<TagRegistry.TagValues<T>> streamFallbackTags(Predicate<Map.Entry<TagKey<T>, Collection<Holder<T>>>> filter) {
		return this.clientOnlyValues.entrySet().stream()
				.filter(filter)
				.map(entry -> new TagRegistry.TagValues<>(entry.getKey(), entry.getValue()));
	}

	@ClientOnly
	public void setFallbackSerializedTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.fallbackSerializedTags = serializedTags;

		if (this.status.isReady()) {
			// Actually apply if the values are ready to be fetched.
			this.applyFallbackTags(this.fallbackSerializedTags);
		} else {
			this.applyFallbackTags(Collections.emptyMap());
		}
	}

	@SuppressWarnings("unchecked")
	@ClientOnly
	private void applyFallbackTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.fallbackValues = this.buildDynamicGroup(serializedTags, TagType.CLIENT_FALLBACK);
		this.bindTags(this.fallbackValues, (ref, tags) -> ((QuiltHolderReferenceHooks<T>) ref).quilt$setFallbackTags(tags));
	}

	@ClientOnly
	public Map<Identifier, List<TagGroupLoader.EntryWithSource>> load(ResourceManager resourceManager) {
		return this.loader.loadTags(resourceManager);
	}

	/**
	 * Called when the source of truth for dynamic registries change.
	 *
	 * @param lookupProvider the lookup for dynamic game objects
	 * @param status         the source of change ({@link ClientRegistryStatus#REMOTE} if triggered by server-provided registries through network,
	 *                       or {@link ClientRegistryStatus#LOCAL} if triggered by data-pack loading to create a new world)
	 */
	@ClientOnly
	public void apply(HolderLookup.Provider lookupProvider, ClientRegistryStatus status) {
		// Prevent overriding the tags if the internal server decides to change registries for some reason,
		// what's sent to the client has higher priority.
		if ((status == ClientRegistryStatus.REMOTE && this.status == status) || this.status != ClientRegistryStatus.REMOTE) {
			this.lookupProvider = lookupProvider;

			this.setSerializedTags(this.serializedTags);
			this.setFallbackSerializedTags(this.fallbackSerializedTags);

			this.status = status;
		}
	}

	@ClientOnly
	public void resetDynamic(boolean force) {
		if (force || this.status == ClientRegistryStatus.LOCAL) {
			this.apply(VANILLA_PROVIDERS, force ? ClientRegistryStatus.REMOTE : ClientRegistryStatus.LOCAL);

			if (force) {
				this.status = ClientRegistryStatus.WAITING;
			}
		}
	}

	@ClientOnly
	private Map<TagKey<T>, Collection<Holder<T>>> buildDynamicGroup(Map<Identifier, List<TagGroupLoader.EntryWithSource>> tagBuilders, TagType type) {
		if (TagRegistryImpl.isRegistryDynamic(this.registryKey)) {
			var tags = new Object2ObjectOpenHashMap<TagKey<T>, Collection<Holder<T>>>();
			var built = this.loader.build(tagBuilders);
			built.forEach((id, tag) -> tags.put(QuiltTagKey.of(this.registryKey, id, type), tag));
			return tags;
		}

		var resolver = new TagResolver(type);
		var sorter = new DependencySorter<Identifier, TagGroupLoader.SortingEntry>();
		tagBuilders.forEach((key, values) -> sorter.addEntry(key, new TagGroupLoader.SortingEntry(values)));
		sorter.buildOrdered(resolver.getCollector());
		return resolver.getTags();
	}

	@ClientOnly
	public void bindTags(Map<TagKey<T>, Collection<Holder<T>>> map, BiConsumer<Holder.Reference<T>, List<TagKey<T>>> consumer) {
		var registry = this.lookupProvider.getLookup(this.registryKey);

		if (registry.isEmpty()) {
			return;
		}

		var boundTags = new IdentityHashMap<Holder.Reference<T>, List<TagKey<T>>>();
		registry.get().holders().forEach(reference -> boundTags.put(reference, new ArrayList<>()));

		map.forEach((tagKey, tag) -> {
			for (var holder : tag) {
				if (!(holder instanceof Holder.Reference<T> reference)) {
					throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagKey);
				}

				boundTags.computeIfAbsent(reference, h -> new ArrayList<>()).add(tagKey);
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	@ClientOnly
	static void init() {
		// Add up all known static registries.
		Registries.REGISTRY.forEach(registry -> {
			get(registry.getKey());
		});

		// Add up known synced dynamic registries.
		DynamicRegistrySyncAccessor.quilt$getSyncableRegistries().forEach((registry, o) -> get((RegistryKey) registry));
	}

	static void forEach(Consumer<ClientTagRegistryManager<?>> consumer) {
		TAG_GROUP_MANAGERS.values().forEach(consumer);
	}

	@ClientOnly
	public static void applyAll(HolderLookup.Provider lookupProvider, ClientRegistryStatus status) {
		TAG_GROUP_MANAGERS.forEach((registryKey, manager) -> manager.apply(lookupProvider, status));
	}

	@ClientOnly
	public static void resetDynamicAll(boolean force) {
		TAG_GROUP_MANAGERS.forEach((registryKey, manager) -> manager.resetDynamic(force));
	}

	@ClientOnly
	private class TagResolver implements TagEntry.Lookup<Holder<T>> {
		private final Map<TagKey<T>, Collection<Holder<T>>> tags = new Object2ObjectOpenHashMap<>();
		private final TagType type;

		private TagResolver(TagType type) {
			this.type = type;
		}

		@Override
		public @Nullable Holder<T> getElement(Identifier id) {
			return ClientTagRegistryManager.this.registryFetcher.apply(id).orElse(null);
		}

		@Override
		public @Nullable Collection<Holder<T>> getTag(Identifier id) {
			return this.tags.get(QuiltTagKey.of(ClientTagRegistryManager.this.registryKey, id, this.type));
		}

		public BiConsumer<Identifier, TagGroupLoader.SortingEntry> getCollector() {
			return (tagId, builder) -> this.tags.put(
					QuiltTagKey.of(ClientTagRegistryManager.this.registryKey, tagId, this.type),
					this.buildLenientTag(builder.entries())
			);
		}

		private Collection<Holder<T>> buildLenientTag(List<TagGroupLoader.EntryWithSource> tagBuilder) {
			ImmutableSet.Builder<Holder<T>> builder = ImmutableSet.builder();

			tagBuilder.forEach(trackedEntry -> trackedEntry.entry().build(this, builder::add));

			return builder.build();
		}

		public Map<TagKey<T>, Collection<Holder<T>>> getTags() {
			return this.tags;
		}
	}

	private abstract class RegistryFetcher implements Function<Identifier, Optional<? extends Holder<T>>> {}

	private class StaticRegistryFetcher extends RegistryFetcher {
		private final RegistryLookup<T> cached;

		private StaticRegistryFetcher() {
			this.cached = ClientTagRegistryManager.this.lookupProvider.getLookupOrThrow(ClientTagRegistryManager.this.registryKey);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Optional<? extends Holder<T>> apply(Identifier id) {
			return this.cached.getHolder(RegistryKey.of((RegistryKey<? extends Registry<T>>) this.cached.getKey(), id));
		}
	}

	/**
	 * Represents a registry content fetcher.
	 * <p>
	 * This fetcher will auto-update the reference to the underlying registry whenever the dynamic registry manager changes.
	 */
	private class ClientRegistryFetcher extends RegistryFetcher {
		private boolean firstCall = true;
		private HolderLookup.Provider lastLookupProvider;
		private RegistryLookup<T> cached;

		private ClientRegistryFetcher() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public Optional<? extends Holder<T>> apply(Identifier id) {
			if (this.firstCall || ClientTagRegistryManager.this.lookupProvider != this.lastLookupProvider) {
				this.lastLookupProvider = ClientTagRegistryManager.this.lookupProvider;

				if (this.lastLookupProvider != null) {
					this.cached = this.lastLookupProvider.getLookup(ClientTagRegistryManager.this.registryKey)
							.orElse(null);
					this.firstCall = false;
				}
			}

			if (this.cached == null) {
				return Optional.empty();
			} else {
				return this.cached.getHolder(RegistryKey.of((RegistryKey<? extends Registry<T>>) this.cached.getKey(), id));
			}
		}
	}
}
