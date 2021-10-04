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

package org.quiltmc.qsl.registry.attribute.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("ClassCanBeRecord")
@ApiStatus.Internal
public final class RegistryEntryAttributeReloader implements SimpleResourceReloader<RegistryEntryAttributeReloader.LoadedData> {
	public static void register(ResourceType source) {
		ResourceLoader.get(source).registerReloader(new RegistryEntryAttributeReloader(source));
	}

	private static final Logger LOGGER = LogManager.getLogger("AttributeReloader");
	private static final Identifier ID = new Identifier("quilt", "attributes");

	private final ResourceType source;

	private RegistryEntryAttributeReloader(ResourceType source) {
		this.source = source;
	}

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public CompletableFuture<LoadedData> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<RegistryEntryAttribute<?, ?>, AttributeMap> attributeMaps = new HashMap<>();

			for (var entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();
				String path = registryId.getNamespace() + "/" + registryId.getPath();
				profiler.push(ID + "/finding_resources/" + path);

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

			if (!attrib.getSide().shouldLoad(source)) {
				LOGGER.warn("Ignoring attribute {} (from {}) since it shouldn't be loaded from this source ({}, we're loading from {})",
						attribId, jsonId, attrib.getSide().getSource(), source);
			}

			profiler.swap(ID + "/getting_resources{" + jsonId + "}");

			List<Resource> resources;
			try {
				resources = manager.getAllResources(jsonId);
			} catch (IOException e) {
				LOGGER.error("Failed to get all resources for {}", jsonId);
				LOGGER.catching(Level.ERROR, e);
				continue;
			}

			profiler.swap(ID + "/processing_resources{" + jsonId + "," + attribId + "}");

			AttributeMap attribMap = attributeMaps.computeIfAbsent(attrib,
					id -> new AttributeMap(registry, attrib));
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
			profiler.push(ID + "/clear_attributes");

			for (var entry : Registry.REGISTRIES.getEntries()) {
				getHolder(entry.getValue()).clear();
			}

			for (Map.Entry<RegistryEntryAttribute<?, ?>, AttributeMap> entry : attributeMaps.entrySet()) {
				profiler.swap(ID + "/apply_attribute{" + entry.getKey().getId() + "}");
				applyOne(entry.getKey(), entry.getValue());
			}

			profiler.pop();
		}

		@SuppressWarnings("unchecked")
		private <R, V> void applyOne(RegistryEntryAttribute<R, V> attrib, AttributeMap attribMap) {
			var registry = attrib.getRegistry();
			Objects.requireNonNull(registry, "registry");

			RegistryEntryAttributeHolder<R> holder = getHolder(registry);
			for (Map.Entry<Identifier, Object> attribEntry : attribMap.map.entrySet()) {
				R item = registry.get(attribEntry.getKey());
				holder.putValue(attrib, item, (V) attribEntry.getValue());
			}
		}
	}

	protected static final class AttributeMap {
		private final Registry<?> registry;
		private final RegistryEntryAttribute<?, ?> attribute;
		public final Map<Identifier, Object> map;

		public AttributeMap(Registry<?> registry, RegistryEntryAttribute<?, ?> attribute) {
			this.registry = registry;
			this.attribute = attribute;
			map = new HashMap<>();
		}

		public void processResource(Resource resource) {
			try {
				boolean replace;
				JsonObject values;

				try {
					JsonObject obj = JsonHelper.deserialize(new InputStreamReader(resource.getInputStream()));
					replace = JsonHelper.getBoolean(obj, "replace", false);
					values = JsonHelper.getObject(obj, "values");
				} catch (JsonSyntaxException e) {
					LOGGER.error("Invalid JSON file " + resource.getId() + ", ignoring", e);
					return;
				}

				// if "replace" is true, the data file wants us to clear all entries from other files before it
				if (replace) {
					map.clear();
				}

				for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
					// keys are IDs for items in the registry
					// therefore, we check that keys are A) valid identifiers, and B) contained in the registry
					Identifier id;
					try {
						id = new Identifier(entry.getKey());
					} catch (InvalidIdentifierException e) {
						LOGGER.error("Invalid identifier in values of {}: '{}', ignoring",
								resource.getId(), entry.getKey());
						LOGGER.catching(Level.ERROR, e);
						continue;
					}
					if (!registry.containsId(id)) {
						LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resource.getId(), id);
						continue;
					}

					// values are deserialized via the attribute's associated Codec instance
					DataResult<?> parsedValue = attribute.getCodec().parse(JsonOps.INSTANCE, entry.getValue());
					if (parsedValue.result().isEmpty()) {
						if (parsedValue.error().isPresent()) {
							LOGGER.error("Failed to parse value for attribute {} of registry entry {}: {}",
									attribute.getId(), id, parsedValue.error().get().message());
						} else {
							LOGGER.error("Failed to parse value for attribute {} of registry entry {}: unknown error",
									attribute.getId(), id);
						}
						LOGGER.error("Ignoring attribute value for '{}' in {} since it's invalid", id, resource.getId());
						continue;
					}

					map.put(id, parsedValue.result().get());
				}
			} catch (Exception e) {
				LOGGER.error("Exception occurred while parsing " + resource.getId() + "!", e);
			}
		}
	}
}
