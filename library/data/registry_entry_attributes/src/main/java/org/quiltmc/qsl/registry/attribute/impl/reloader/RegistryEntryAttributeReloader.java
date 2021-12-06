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

package org.quiltmc.qsl.registry.attribute.impl.reloader;

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

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.impl.AssetsHolderGuard;
import org.quiltmc.qsl.registry.attribute.impl.Initializer;
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolder;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.mixin.RequiredTagListRegistryAccessor;

@ApiStatus.Internal
public final class RegistryEntryAttributeReloader implements SimpleResourceReloader<RegistryEntryAttributeReloader.LoadedData>,
		TagGetter {
	public static void register(ResourceType source) {
		ResourceLoader.get(source).registerReloader(new RegistryEntryAttributeReloader(source));
	}

	static final Logger LOGGER = LogManager.getLogger("AttributeReloader");
	private static final Identifier ID_DATA = new Identifier(Initializer.NAMESPACE, "data");
	private static final Identifier ID_ASSETS = new Identifier(Initializer.NAMESPACE, "assets");

	private final ResourceType source;
	private final Identifier id;
	private final Map<RegistryKey<?>, TagGroup<?>> registryTagGroupCache;
	private final Set<RegistryKey<?>> erroredNoTagList;

	private RegistryEntryAttributeReloader(ResourceType source) {
		if (source == ResourceType.CLIENT_RESOURCES) {
			AssetsHolderGuard.assertAccessAllowed();
		}
		this.source = source;
		id = switch (source) {
			case SERVER_DATA -> ID_DATA;
			case CLIENT_RESOURCES -> ID_ASSETS;
		};
		registryTagGroupCache = new HashMap<>();
		erroredNoTagList = new HashSet<>();
	}

	@Override
	public Identifier getQuiltId() {
		return id;
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
				LOGGER.error("Tried to use tag in attribute, but {} isn't configured to have tags!",
						registryKey.getValue());
			}
			return null;
		} else {
			registryTagGroupCache.put(registryKey, group);
			var tag = group.getTag(id);
			if (source == ResourceType.SERVER_DATA) {
				var qTag = QuiltTag.getExtensions(tag);
				if (qTag.getType() == TagType.CLIENT_ONLY) {
					LOGGER.error("Tried to use client-only tag {} in non-client-only attribute!", id);
					return null;
				}
			}
			return tag;
		}
	}

	@Override
	public CompletableFuture<LoadedData> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<RegistryEntryAttribute<?, ?>, AttributeMap> attributeMaps = new HashMap<>();

			for (var entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();
				String path = registryId.getNamespace() + "/" + registryId.getPath();
				profiler.push(id + "/finding_resources/" + path);

				Collection<Identifier> jsonIds = manager.findResources("attributes/" + path, s -> s.endsWith(".json"));
				if (jsonIds.isEmpty()) {
					continue;
				}

				Registry<?> registry = entry.getValue();
				processResources(manager, profiler, attributeMaps, jsonIds, registry);

				profiler.pop();
			}

			return new LoadedData(attributeMaps);
		}, executor);
	}

	private void processResources(ResourceManager manager, Profiler profiler,
								  Map<RegistryEntryAttribute<?, ?>, AttributeMap> attributeMaps,
								  Collection<Identifier> jsonIds, Registry<?> registry) {
		for (var jsonId : jsonIds) {
			Identifier attribId = getAttributeId(jsonId);
			RegistryEntryAttribute<?, ?> attrib = RegistryEntryAttributeHolder.getAttribute(registry, attribId);
			if (attrib == null) {
				LOGGER.warn("Unknown attribute {} (from {})", attribId, jsonId);
				continue;
			}

			if (!attrib.side().shouldLoad(source)) {
				LOGGER.warn("Ignoring attribute {} (from {}) since it shouldn't be loaded from this source ({}, we're loading from {})",
						attribId, jsonId, attrib.side().getSource(), source);
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

			profiler.swap(id + "/processing_resources{" + jsonId + "," + attribId + "}");

			AttributeMap attribMap = attributeMaps.computeIfAbsent(attrib,
					key -> new AttributeMap(registry, key, this));
			for (var resource : resources) {
				attribMap.processResource(resource);
			}
		}
	}

	@Override
	public CompletableFuture<Void> apply(LoadedData data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> data.apply(profiler), executor);
	}

	// "<namespace>:attributes/<path>/<file_name>.json" becomes "<namespace>:<file_name>"
	private Identifier getAttributeId(Identifier jsonId) {
		String path = jsonId.getPath();
		int lastSlash = path.lastIndexOf('/');
		path = path.substring(lastSlash + 1);

		int lastDot = path.lastIndexOf('.');
		path = path.substring(0, lastDot);
		return new Identifier(jsonId.getNamespace(), path);
	}

	private <R> RegistryEntryAttributeHolder<R> getHolder(Registry<R> registry) {
		return switch (source) {
			case CLIENT_RESOURCES -> RegistryEntryAttributeHolder.getAssets(registry);
			case SERVER_DATA -> RegistryEntryAttributeHolder.getData(registry);
		};
	}

	protected final class LoadedData {
		private final Map<RegistryEntryAttribute<?, ?>, AttributeMap> attributeMaps;

		private LoadedData(Map<RegistryEntryAttribute<?, ?>, AttributeMap> attributeMaps) {
			this.attributeMaps = attributeMaps;
		}

		public void apply(Profiler profiler) {
			profiler.push(id + "/clear_attributes");

			for (var entry : Registry.REGISTRIES.getEntries()) {
				getHolder(entry.getValue()).clear();
			}

			for (Map.Entry<RegistryEntryAttribute<?, ?>, AttributeMap> entry : attributeMaps.entrySet()) {
				profiler.swap(id + "/apply_attribute{" + entry.getKey().id() + "}");
				applyOne(entry.getKey(), entry.getValue());
			}

			profiler.pop();
		}

		@SuppressWarnings("unchecked")
		private <R, V> void applyOne(RegistryEntryAttribute<R, V> attrib, AttributeMap attribMap) {
			var registry = attrib.registry();
			Objects.requireNonNull(registry, "registry");

			RegistryEntryAttributeHolder<R> holder = getHolder(registry);
			for (Map.Entry<AttributeTarget, Object> attribEntry : attribMap.getMap().entrySet()) {
				V value = (V) attribEntry.getValue();
				try {
					for (Identifier id : attribEntry.getKey().ids()) {
						R item = registry.get(id);
						holder.putValue(attrib, item, value);
					}
				} catch (AttributeTarget.ResolveException e) {
					// TODO handle this better, somehow??
					LOGGER.error("Failed to apply values for attribute {}!", attrib.id());
					LOGGER.catching(Level.ERROR, e);
					break;
				}
			}
		}
	}
}
