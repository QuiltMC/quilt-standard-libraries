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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AttributeReloader implements SimpleResourceReloader<Map<RegistryItemAttribute<?, ?>, AttributeReloader.AttributeMap>> {
	private static final Logger LOGGER = LogManager.getLogger("AttributeReloader");
	private static final Identifier ID = new Identifier("quilt", "attributes");

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public CompletableFuture<Map<RegistryItemAttribute<?, ?>, AttributeMap>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			final Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps = new HashMap<>();
			for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();
				StringBuilder pathSB = new StringBuilder();
				if (!"minecraft".equals(registryId.getNamespace()))
					pathSB.append(registryId.getNamespace()).append('/');
				pathSB.append(registryId.getPath());
				profiler.push(ID + "/finding_resources/" + pathSB);
				Collection<Identifier> jsonIds = manager.findResources("attributes/" + pathSB, s -> s.endsWith(".json"));
				if (jsonIds.isEmpty())
					continue;
				profiler.pop();
				RegistryItemAttributeHolderImpl<?> holder = RegistryItemAttributeHolderImpl.get(entry.getValue());
				for (Identifier jsonId : jsonIds) {
					Identifier attribId = getAttributeId(jsonId);
					RegistryItemAttribute<?, ?> attrib = holder.getAttribute(attribId);
					if (attrib == null) {
						LOGGER.warn("Unknown attribute {} (from {})", attribId, jsonId);
						continue;
					}
					profiler.push(ID + "/getting_resources{" + jsonId + "}");
					List<Resource> resources;
					try {
						resources = manager.getAllResources(jsonId);
					} catch (IOException e) {
						LOGGER.error("Failed to get all resources for {}", jsonId);
						LOGGER.catching(Level.ERROR, e);
						continue;
					}
					AttributeMap attribMap = attributeMaps.computeIfAbsent(attrib,
							id -> new AttributeMap(entry.getValue(), attrib));
					profiler.swap(ID + "/processing_resources{" + jsonId + "," + attribId + "}");
					for (Resource resource : resources) {
						attribMap.processResource(resource);
					}
					profiler.pop();
				}
			}
			return attributeMaps;
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Map<RegistryItemAttribute<?, ?>, AttributeMap> data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			for (Map.Entry<RegistryItemAttribute<?, ?>, AttributeMap> entry : data.entrySet()) {
				profiler.push(ID + "/apply_attribute{" + entry.getKey().getId() + "}");
				applyOne(entry.getKey(), entry.getValue());
				profiler.pop();
			}
		}, executor);
	}

	@SuppressWarnings("unchecked")
	private <R, T> void applyOne(RegistryItemAttribute<R, T> attrib, AttributeMap attribMap) {
		Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(attrib.getRegistryKey().getValue());
		assert registry != null : "huh";
		RegistryItemAttributeHolderImpl<R> holder = RegistryItemAttributeHolderImpl.get(registry);
		for (Map.Entry<Identifier, Object> attribEntry : attribMap.map.entrySet()) {
			R item = registry.get(attribEntry.getKey());
			holder.putValue(item, attrib, (T) attribEntry.getValue());
		}
	}

	private Identifier getAttributeId(Identifier jsonId) {
		String path = jsonId.getPath();
		int lastSlash = path.lastIndexOf('/');
		path = path.substring(lastSlash + 1);
		int lastDot = path.lastIndexOf('.');
		path = path.substring(0, lastDot);
		return new Identifier(jsonId.getNamespace(), path);
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
			JsonObject obj = JsonHelper.deserialize(new InputStreamReader(resource.getInputStream()));
			boolean replace = JsonHelper.getBoolean(obj, "replace", false);
			JsonObject values = JsonHelper.getObject(obj, "values");
			if (values == null) {
				LOGGER.error("Missing 'values' element in {}, ignoring file", resource.getId());
				return;
			}
			if (replace)
				map.clear();
			for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
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
				map.put(id, parsedValue.result().get());
			}
		}
	}
}
