package org.quiltmc.qsl.registry.attribute.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AttributeReloader implements SimpleSynchronousResourceReloader {
	private static final Logger LOGGER = LogManager.getLogger("AttributeReloader");
	private static final Identifier ID = new Identifier("quilt", "attributes");

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps = load(manager);
		apply(attributeMaps);
	}

	private Map<RegistryItemAttribute<?, ?>, AttributeMap> load(ResourceManager manager) {
		final Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps = new HashMap<>();
		for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registry.REGISTRIES.getEntries()) {
			Identifier registryId = entry.getKey().getValue();
			StringBuilder pathSB = new StringBuilder("attributes/");
			if (!"minecraft".equals(registryId.getNamespace()))
				pathSB.append(registryId.getNamespace()).append('/');
			pathSB.append(registryId.getPath());
			Collection<Identifier> jsonIds = manager.findResources(pathSB.toString(), s -> s.endsWith(".json"));
			if (jsonIds.isEmpty())
				continue;
			RegistryItemAttributeHolderImpl<?> holder = RegistryItemAttributeHolderImpl.get(entry.getValue());
			for (Identifier jsonId : jsonIds) {
				Identifier attribId = getAttributeId(jsonId);
				RegistryItemAttribute<?, ?> attrib = holder.getAttribute(attribId);
				if (attrib == null) {
					LOGGER.warn("Unknown attribute {} (from {})", attribId, jsonId);
					continue;
				}
				List<Resource> resources;
				try {
					resources = manager.getAllResources(jsonId);
				} catch (IOException e) {
					LOGGER.error("Failed to get all resources for {}", jsonId);
					LOGGER.catching(Level.ERROR, e);
					continue;
				}
				AttributeMap attribMap = attributeMaps.computeIfAbsent(attrib, id -> new AttributeMap(entry.getValue()));
				for (Resource resource : resources) {
					attribMap.processResource(resource);
				}
			}
		}
		return attributeMaps;
	}

	private void apply(Map<RegistryItemAttribute<?, ?>, AttributeMap> attributeMaps) {
		for (Map.Entry<RegistryItemAttribute<?, ?>, AttributeMap> entry : attributeMaps.entrySet()) {
			applyOne(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private <R, T> void applyOne(RegistryItemAttribute<R, T> attrib, AttributeMap attribMap) {
		Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(attrib.getRegistryKey().getValue());
		RegistryItemAttributeHolderImpl<R> holder = RegistryItemAttributeHolderImpl.get(registry);
		for (Map.Entry<Identifier, JsonElement> attribEntry : attribMap.map.entrySet()) {
			R item = registry.get(attribEntry.getKey());
			DataResult<T> value = attrib.getCodec().parse(JsonOps.INSTANCE, attribEntry.getValue());
			T result = value.result().orElseThrow(() -> new RuntimeException("aaa failed to deserialize attribute value"));
			holder.putValue(item, attrib, result);
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
		public final Map<Identifier, JsonElement> map;

		public AttributeMap(Registry<?> registry) {
			this.registry = registry;
			map = new HashMap<>();
		}

		public void processResource(Resource rsrc) {
			JsonObject obj = JsonHelper.deserialize(new InputStreamReader(rsrc.getInputStream()));
			boolean replace = JsonHelper.getBoolean(obj, "replace", false);
			JsonObject values = JsonHelper.getObject(obj, "values");
			if (values == null) {
				LOGGER.error("Missing 'values' element in {}, ignoring file", rsrc.getId());
				return;
			}
			if (replace)
				map.clear();
			for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
				Identifier id;
				try {
					id = new Identifier(entry.getKey());
				} catch (InvalidIdentifierException e) {
					LOGGER.error("Invalid identifier in values of {}: '{}', ignoring", rsrc.getId(), entry.getKey());
					LOGGER.catching(Level.ERROR, e);
					continue;
				}
				if (!registry.containsId(id)) {
					LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", rsrc.getId(), id);
				}
				map.put(id, entry.getValue());
			}
		}
	}
}
