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
import java.util.Collection;
import java.util.Collections;
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
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagEntry;
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
	private DynamicRegistryManager registryManager = BuiltinRegistries.MANAGER;
	private Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags = Map.of();
	private Map<TagKey<T>, Collection<Holder<T>>> clientOnlyValues;
	private Map<Identifier, List<TagGroupLoader.EntryWithSource>> fallbackSerializedTags = Map.of();
	private Map<TagKey<T>, Collection<Holder<T>>> fallbackValues;

	private ClientTagRegistryManager(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		this.registryKey = registryKey;
		this.registryFetcher = new ClientRegistryFetcher();
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

	@SuppressWarnings("unchecked")
	@Environment(EnvType.CLIENT)
	public void setSerializedTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.serializedTags = serializedTags;
		this.clientOnlyValues = this.buildDynamicGroup(this.serializedTags, TagType.CLIENT_ONLY);
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

	@SuppressWarnings("unchecked")
	@Environment(EnvType.CLIENT)
	public void setFallbackSerializedTags(Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
		this.fallbackSerializedTags = serializedTags;
		this.fallbackValues = this.buildDynamicGroup(this.fallbackSerializedTags, TagType.CLIENT_FALLBACK);
		this.bindTags(this.fallbackValues, (ref, tags) -> ((QuiltHolderReferenceHooks<T>) ref).quilt$setFallbackTags(tags));
	}

	@Environment(EnvType.CLIENT)
	public Map<Identifier, List<TagGroupLoader.EntryWithSource>> load(ResourceManager resourceManager) {
		return this.loader.loadTags(resourceManager);
	}

	@Environment(EnvType.CLIENT)
	public void apply(DynamicRegistryManager registryManager) {
		this.registryManager = registryManager;

		this.setSerializedTags(this.serializedTags);
		this.setFallbackSerializedTags(this.fallbackSerializedTags);
	}

	@Environment(EnvType.CLIENT)
	private Map<TagKey<T>, Collection<Holder<T>>> buildDynamicGroup(Map<Identifier, List<TagGroupLoader.EntryWithSource>> tagBuilders, TagType type) {
		if (!TagRegistryImpl.isRegistryDynamic(this.registryKey)) {
			var tags = new Object2ObjectOpenHashMap<TagKey<T>, Collection<Holder<T>>>();
			var built = this.loader.build(tagBuilders);
			built.forEach((id, tag) -> tags.put(QuiltTagKey.of(this.registryKey, id, type), tag));
			return tags;
		}

		var resolver = new TagResolver(type);
		Multimap<Identifier, Identifier> tagEntries = HashMultimap.create();

		this.visitDependencies(tagBuilders, (tagId, entry) -> entry.visitRequiredDependencies(
				tagEntryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, tagEntryId)
		));
		this.visitDependencies(tagBuilders, (tagId, entry) -> entry.visitOptionalDependencies(
				tagEntryId -> TagGroupLoaderAccessor.invokeAddDependencyIfNotCyclic(tagEntries, tagId, tagEntryId)
		));

		var set = new HashSet<Identifier>();
		tagBuilders.keySet().forEach(tagId ->
				TagGroupLoaderAccessor.invokeVisitDependenciesAndEntry(tagBuilders, tagEntries, set, tagId,
						resolver.getDependencyConsumer(tagId)
				)
		);
		return resolver.getTags();
	}

	private void visitDependencies(Map<Identifier, List<TagGroupLoader.EntryWithSource>> tagBuilders,
			BiConsumer<Identifier, TagEntry> dependencyConsumer) {
		tagBuilders.forEach((tagId, builder) -> builder.forEach(entry -> dependencyConsumer.accept(tagId, entry.entry())));
	}

	@Environment(EnvType.CLIENT)
	public void bindTags(Map<TagKey<T>, Collection<Holder<T>>> map, BiConsumer<Holder.Reference<T>, List<TagKey<T>>> consumer) {
		var registry = this.registryManager.getOptional(this.registryKey);

		if (registry.isEmpty()) {
			return;
		}

		var boundTags = new IdentityHashMap<Holder.Reference<T>, List<TagKey<T>>>();
		registry.get().holders().forEach(reference -> boundTags.put(reference, new ArrayList<>()));

		map.forEach((tagKey, tag) -> {
			for (var holder : tag) {
				if (!holder.isRegistry(registry.get())) {
					throw new IllegalStateException(
							"Can't create named set " + tagKey + " containing value "
									+ holder + " from outside registry " + registry.get()
					);
				}

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

	@Environment(EnvType.CLIENT)
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

		public BiConsumer<Identifier, List<TagGroupLoader.EntryWithSource>> getDependencyConsumer(Identifier tagId) {
			return (currentTagId, builder) -> this.tags.put(
					QuiltTagKey.of(ClientTagRegistryManager.this.registryKey, tagId, type),
					this.buildLenientTag(builder)
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
				return this.cached.getHolder(RegistryKey.of(this.cached.getKey(), id));
			}
		}
	}
}
