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

package org.quiltmc.qsl.registry.dict.impl.reloader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;
import org.quiltmc.qsl.registry.dict.impl.AssetsHolderGuard;
import org.quiltmc.qsl.registry.dict.impl.Initializer;
import org.quiltmc.qsl.registry.dict.impl.RegistryDictHolder;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.mixin.RequiredTagListRegistryAccessor;

@ApiStatus.Internal
public final class RegistryDictReloader implements SimpleResourceReloader<RegistryDictReloader.LoadedData>,
		TagGetter {
	public static void register(ResourceType source) {
		ResourceLoader.get(source).registerReloader(new RegistryDictReloader(source));
	}

	static final Logger LOGGER = LogManager.getLogger("RegistryDictReloader");
	private static final Identifier ID_DATA = new Identifier(Initializer.NAMESPACE, "data");
	private static final Identifier ID_ASSETS = new Identifier(Initializer.NAMESPACE, "assets");

	private final ResourceType source;
	private final Identifier id;
	private final Collection<Identifier> deps;
	private final Map<RegistryKey<?>, TagGroup<?>> registryTagGroupCache;
	private final Set<RegistryKey<?>> erroredNoTagList;

	private RegistryDictReloader(ResourceType source) {
		if (source == ResourceType.CLIENT_RESOURCES) {
			AssetsHolderGuard.assertAccessAllowed();
		}
		this.source = source;
		id = switch (source) {
			case SERVER_DATA -> ID_DATA;
			case CLIENT_RESOURCES -> ID_ASSETS;
		};
		deps = switch (source) {
			case SERVER_DATA -> Set.of(ResourceReloaderKeys.Server.TAGS);
			case CLIENT_RESOURCES -> Set.of(ResourceReloaderKeys.Server.TAGS,
					new Identifier("quilt_tags", "client_only_tags"));
		};
		registryTagGroupCache = new HashMap<>();
		erroredNoTagList = new HashSet<>();
	}

	@Override
	public Identifier getQuiltId() {
		return id;
	}

	@Override
	public Collection<Identifier> getQuiltDependencies() {
		return deps;
	}

	@SuppressWarnings({"unchecked", "ConstantConditions"})
	@Override
	public <T> Tag<T> getTag(RegistryKey<? extends Registry<T>> registryKey, Identifier id) {
		if (erroredNoTagList.contains(registryKey)) {
			return null;
		}

		var group = (TagGroup<T>) registryTagGroupCache.get(registryKey);
		if (group == null) {
			for (var entry : RequiredTagListRegistryAccessor.getAll()) {
				if (entry.getRegistryKey() == registryKey) {
					group = (TagGroup<T>) entry.getGroup();
					break;
				}
			}
		}

		if (group == null) { // suppressed because IDEA is too optimistic and thinks the loop above can't fail
			if (!erroredNoTagList.add(registryKey)) {
				LOGGER.error("Tried to use tag in dictionary, but {} isn't configured to have tags!",
						registryKey.getValue());
			}
			return null;
		} else {
			registryTagGroupCache.put(registryKey, group);
			var tag = group.getTag(id);
			if (source == ResourceType.SERVER_DATA) {
				var qTag = QuiltTag.getExtensions(tag);
				if (qTag.getType() == TagType.CLIENT_ONLY) {
					LOGGER.error("Tried to use client-only tag {} in non-client-only dictionary!", id);
					return null;
				}
			}
			return tag;
		}
	}

	@Override
	public CompletableFuture<LoadedData> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<RegistryDict<?, ?>, DictMap> dictionaryMaps = new HashMap<>();

			for (var entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();
				String path = registryId.getNamespace() + "/" + registryId.getPath();
				profiler.push(id + "/finding_resources/" + path);

				Collection<Identifier> jsonIds = manager.findResources("dicts/" + path, s -> s.endsWith(".json"));
				if (jsonIds.isEmpty()) {
					continue;
				}

				Registry<?> registry = entry.getValue();
				processResources(manager, profiler, dictionaryMaps, jsonIds, registry);

				profiler.pop();
			}

			return new LoadedData(dictionaryMaps);
		}, executor);
	}

	private void processResources(ResourceManager manager, Profiler profiler,
								  Map<RegistryDict<?, ?>, DictMap> dictionaryMaps,
								  Collection<Identifier> jsonIds, Registry<?> registry) {
		for (var jsonId : jsonIds) {
			Identifier dictId = getDictionaryId(jsonId);
			RegistryDict<?, ?> dict = RegistryDictHolder.getDict(registry, dictId);
			if (dict == null) {
				LOGGER.warn("Unknown dictionary {} (from {})", dictId, jsonId);
				continue;
			}

			if (!dict.side().shouldLoad(source)) {
				LOGGER.warn("Ignoring dictionary {} (from {}) since it shouldn't be loaded from this source ({}, we're loading from {})",
						dictId, jsonId, dict.side().getSource(), source);
				continue;
			}

			profiler.swap(id + "/getting_resources{" + jsonId + "}");

			List<Resource> resources;
			try {
				resources = manager.getAllResources(jsonId);
			} catch (IOException e) {
				LOGGER.error("Failed to get all resources for {}", jsonId);
				LOGGER.catching(Level.ERROR, e);
				continue;
			}

			profiler.swap(id + "/processing_resources{" + jsonId + "," + dictId + "}");

			DictMap dictMap = dictionaryMaps.computeIfAbsent(dict,
					key -> new DictMap(registry, key, this));
			for (var resource : resources) {
				dictMap.processResource(resource);
			}
		}
	}

	@Override
	public CompletableFuture<Void> apply(LoadedData data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> data.apply(profiler), executor);
	}

	// "<namespace>:dicts/<path>/<file_name>.json" becomes "<namespace>:<file_name>"
	private Identifier getDictionaryId(Identifier jsonId) {
		String path = jsonId.getPath();
		int lastSlash = path.lastIndexOf('/');
		path = path.substring(lastSlash + 1);

		int lastDot = path.lastIndexOf('.');
		path = path.substring(0, lastDot);
		return new Identifier(jsonId.getNamespace(), path);
	}

	private <R> RegistryDictHolder<R> getHolder(Registry<R> registry) {
		return switch (source) {
			case CLIENT_RESOURCES -> RegistryDictHolder.getAssets(registry);
			case SERVER_DATA -> RegistryDictHolder.getData(registry);
		};
	}

	protected final class LoadedData {
		private final Map<RegistryDict<?, ?>, DictMap> dictionaryMaps;

		private LoadedData(Map<RegistryDict<?, ?>, DictMap> dictionaryMaps) {
			this.dictionaryMaps = dictionaryMaps;
		}

		public void apply(Profiler profiler) {
			profiler.push(id + "/clear_dicts");

			for (var entry : Registry.REGISTRIES.getEntries()) {
				getHolder(entry.getValue()).clear();
			}

			for (Map.Entry<RegistryDict<?, ?>, DictMap> entry : dictionaryMaps.entrySet()) {
				profiler.swap(id + "/apply_dict{" + entry.getKey().id() + "}");
				applyOne(entry.getKey(), entry.getValue());
			}

			profiler.pop();
		}

		@SuppressWarnings("unchecked")
		private <R, V> void applyOne(RegistryDict<R, V> dict, DictMap dictMap) {
			var registry = dict.registry();
			Objects.requireNonNull(registry, "registry");

			RegistryDictHolder<R> holder = getHolder(registry);
			for (Map.Entry<DictTarget, Object> dictEntry : dictMap.getMap().entrySet()) {
				V value = (V) dictEntry.getValue();
				try {
					for (Identifier id : dictEntry.getKey().ids()) {
						R item = registry.get(id);
						holder.putValue(dict, item, value);
					}
				} catch (DictTarget.ResolveException e) {
					// TODO handle this better, somehow??
					LOGGER.error("Failed to apply values for dictionary {}!", dict.id());
					LOGGER.catching(Level.ERROR, e);
					break;
				}
			}
		}
	}
}
