package org.quiltmc.qsl.registry.attribute.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class RegistryItemAttributeReloader implements SimpleResourceReloader<RegistryItemAttributeReloader.LoadedData> {
	private static final Logger LOGGER = LogManager.getLogger("AttributeReloader");
	private static final Identifier ID = new Identifier("quilt", "attributes");

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public CompletableFuture<LoadedData> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps = new HashMap<>();
			for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();

				// calculate the root path of this registry's attribute maps
				String path = getAttributeMapPath(registryId);
				// find all JSON files that match this registry's attribute map path
				profiler.push(ID + "/finding_resources/" + path);
				Collection<Identifier> jsonIds = manager.findResources("attributes/" + path, s -> s.endsWith(".json"));
				if (jsonIds.isEmpty()) {
					continue;
				}
				profiler.pop();

				// grab the built-in holder so we can see what attributes are registered for this registry
				BuiltinRegistryItemAttributeHolder<?> holder = RegistryItemAttributeHolderImpl.getBuiltin(entry.getValue());
				for (Identifier jsonId : jsonIds) {
					// get the attribute ID from the resource ID
					Identifier attribId = getAttributeId(jsonId);
					// get the matching attribute (fail if it doesn't exist)
					RegistryItemAttribute<?, ?> attrib = holder.getAttribute(attribId);
					if (attrib == null) {
						LOGGER.warn("Unknown attribute {} (from {})", attribId, jsonId);
						continue;
					}

					// get all the resources that match this resource ID
					profiler.push(ID + "/getting_resources{" + jsonId + "}");
					List<Resource> resources;
					try {
						resources = manager.getAllResources(jsonId);
					} catch (IOException e) {
						LOGGER.error("Failed to get all resources for {}", jsonId);
						LOGGER.catching(Level.ERROR, e);
						continue;
					}

					// grab an AttributeMap (utility structure that stores deserialized data), creating one if needed
					AttributeMap attribMap = attributeMaps.computeIfAbsent(attrib,
							id -> new AttributeMap(entry.getValue(), attrib));
					// finally, tell AttributeMap "hey, process these resources for me please"
					profiler.swap(ID + "/processing_resources{" + jsonId + "," + attribId + "}");
					for (Resource resource : resources) {
						attribMap.processResource(resource);
					}
					profiler.pop();
				}
			}
			return new LoadedData(attributeMaps);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(LoadedData data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> data.apply(profiler), executor);
	}

	// "attributes/<registry_path>" for Minecraft registries,
	// "attributes/<registry_namespace>/<registry_path>" for modded ones
	private String getAttributeMapPath(Identifier registryId) {
		String path = registryId.getPath();
		if (!"minecraft".equals(registryId.getNamespace()))
			path = registryId.getNamespace() + "/" + path;
		return path;
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

	protected record LoadedData(Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps) {
		public void apply(Profiler profiler) {
			// clear all attribute values from all registries, since we're reloading them now
			profiler.push(ID + "/clear_attributes");
			for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registry.REGISTRIES.getEntries()) {
				RegistryItemAttributeHolderImpl.getData(entry.getValue()).clear();
			}

			// apply all our AttributeMap values!
			for (Map.Entry<RegistryItemAttribute<?, ?>, AttributeMap> entry : attributeMaps.entrySet()) {
				profiler.swap(ID + "/apply_attribute{" + entry.getKey().getId() + "}");
				applyOne(entry.getKey(), entry.getValue());
			}
			profiler.pop();
		}

		@SuppressWarnings("unchecked")
		private <R, T> void applyOne(RegistryItemAttribute<R, T> attrib, AttributeMap attribMap) {
			Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(attrib.getRegistryKey().getValue());
			assert registry != null : "huh";

			BuiltinRegistryItemAttributeHolder<R> holder = RegistryItemAttributeHolderImpl.getData(registry);
			for (Map.Entry<Identifier, Object> attribEntry : attribMap.map.entrySet()) {
				R item = registry.get(attribEntry.getKey());
				holder.putValue(item, attrib, (T) attribEntry.getValue());
			}
		}
	}

	protected static final class AttributeMap {
		private final Registry<?> registry;
		private final RegistryItemAttribute<?, ?> attribute;
		public final Map<Identifier, Object> map;

		public AttributeMap(Registry<?> registry, RegistryItemAttribute<?, ?> attribute) {
			this.registry = registry;
			this.attribute = attribute;
			map = new HashMap<>();
		}

		public void processResource(Resource resource) {
			// deserialize the resource
			JsonObject obj = JsonHelper.deserialize(new InputStreamReader(resource.getInputStream()));
			// yoink some values
			boolean replace = JsonHelper.getBoolean(obj, "replace", false);
			JsonObject values = JsonHelper.getObject(obj, "values");
			if (values == null) {
				LOGGER.error("Missing 'values' element in {}, ignoring file", resource.getId());
				return;
			}

			// if "replace" is true, the data file wants us to clear _all_ entries from other files before it.
			// so, do that!
			if (replace) {
				map.clear();
			}
			// start inspecting the "values" object
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

				// values are... well, values
				// we use Mojang's weird optical hacks to parse them from JSON
				DataResult<?> parsedValue = attribute.getCodec().parse(JsonOps.INSTANCE, entry.getValue());
				if (parsedValue.result().isEmpty()) {
					if (parsedValue.error().isPresent()) {
						LOGGER.error("Failed to parse value for attribute {} of registry item {}: {}",
								attribute.getId(), id, parsedValue.error().get().message());
					} else {
						LOGGER.error("Failed to parse value for attribute {} of registry item {}: unknown error",
								attribute.getId(), id);
					}
					LOGGER.error("Ignoring attribute value for '{}' in {} since it's invalid", id, resource.getId());
				}

				// we got both a key and a value? great, throw them on the map!
				map.put(id, parsedValue.result().get());
			}
		}
	}
}
