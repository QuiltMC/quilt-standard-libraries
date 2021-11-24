package org.quiltmc.qsl.registry.attribute.impl.reloader;

import static org.quiltmc.qsl.registry.attribute.impl.reloader.RegistryEntryAttributeReloader.LOGGER;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

final class AttributeMap {
	@FunctionalInterface
	public interface TagTargetFactory {
		<T> AttributeTarget.Tag<T> create(Registry<T> registry, Identifier tagId);
	}

	private final TagTargetFactory tagTargetFactory;
	private final Registry<?> registry;
	private final RegistryEntryAttribute<?, ?> attribute;
	private Map<AttributeTarget, Object> map;

	public AttributeMap(TagTargetFactory tagTargetFactory, Registry<?> registry, RegistryEntryAttribute<?, ?> attribute) {
		this.tagTargetFactory = tagTargetFactory;
		this.registry = registry;
		this.attribute = attribute;
		map = new HashMap<>();
	}

	public void put(Identifier id, Object value) {
		map.put(new AttributeTarget.Single(id), value);
	}

	public void putTag(Identifier tagId, Object value) {
		map.put(tagTargetFactory.create(registry, tagId), value);
	}

	public Registry<?> getRegistry() {
		return registry;
	}

	public RegistryEntryAttribute<?, ?> getAttribute() {
		return attribute;
	}

	public Map<AttributeTarget, Object> getMap() {
		return map;
	}

	private Object parseValue(Resource resource, Identifier id, JsonElement value) {
		DataResult<?> parsedValue = attribute.codec().parse(JsonOps.INSTANCE, value);
		if (parsedValue.result().isEmpty()) {
			if (parsedValue.error().isPresent()) {
				LOGGER.error("Failed to parse value for attribute {} of registry entry {}: {}",
						attribute.id(), id, parsedValue.error().get().message());
			} else {
				LOGGER.error("Failed to parse value for attribute {} of registry entry {}: unknown error",
						attribute.id(), id);
			}
			LOGGER.error("Ignoring attribute value for '{}' in {} since it's invalid", id, resource.getId());
			return null;
		}
		return parsedValue.result().get();
	}

	// FIXME tag support!
	// FIXME rewrite this to be less messy
	public void processResource(Resource resource) {
		try {
			boolean replace;
			JsonElement values;

			try {
				JsonObject obj = JsonHelper.deserialize(new InputStreamReader(resource.getInputStream()));
				replace = JsonHelper.getBoolean(obj, "replace", false);
				values = obj.get("values");
				if (values == null) {
					throw new JsonSyntaxException("Missing values, expected to find a JsonArray or JsonObject");
				} else if (!values.isJsonArray() && !values.isJsonObject()) {
					throw new JsonSyntaxException("Expected values to be a JsonArray or JsonObject," +
							"was " + JsonHelper.getType(values));
				}
			} catch (JsonSyntaxException e) {
				LOGGER.error("Invalid JSON file " + resource.getId() + ", ignoring", e);
				return;
			}

			// if "replace" is true, the data file wants us to clear all entries from other files before it
			if (replace) {
				map.clear();
			}

			if (values.isJsonArray()) {
				JsonArray valuesA = values.getAsJsonArray();

				int index = 0;
				for (JsonElement entry : valuesA) {
					if (!entry.isJsonObject()) {
						LOGGER.error("Invalid element at index {} in values of {}: expected a JsonObject, was {}",
								index, resource.getId(), JsonHelper.getType(entry));
						index++;
						continue;
					}
					JsonObject entryO = entry.getAsJsonObject();
					Identifier id;
					JsonElement value;
					boolean required;

					try {
						String idStr = JsonHelper.getString(entryO, "id");
						id = new Identifier(idStr);
					} catch (JsonSyntaxException e) {
						LOGGER.error("Invalid element at index {} in values of {}: syntax error",
								index, resource.getId());
						LOGGER.catching(Level.ERROR, e);
						index++;
						continue;
					} catch (InvalidIdentifierException e) {
						LOGGER.error("Invalid element at index {} in values of {}: invalid identifier",
								index, resource.getId());
						LOGGER.catching(Level.ERROR, e);
						index++;
						continue;
					}

					try {
						value = entryO.get("value");
						if (value == null) {
							throw new JsonSyntaxException("Missing value");
						}
					} catch (JsonSyntaxException e) {
						LOGGER.error("Failed to parse value for registry entry {} in values of {}: syntax error",
								id, resource.getId());
						LOGGER.catching(Level.ERROR, e);
						index++;
						continue;
					}

					required = JsonHelper.getBoolean(entryO, "required", true);

					if (required && !registry.containsId(id)) {
						LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resource.getId(), id);
						index++;
						continue;
					}

					Object parsedValue = parseValue(resource, id, value);
					if (parsedValue == null) {
						index++;
						continue;
					}

					put(id, parsedValue);

					index++;
				}
			} else if (values.isJsonObject()) {
				JsonObject valuesO = values.getAsJsonObject();

				for (Map.Entry<String, JsonElement> entry : valuesO.entrySet()) {
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

					Object parsedValue = parseValue(resource, id, entry.getValue());
					if (parsedValue == null) {
						continue;
					}

					put(id, parsedValue);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing " + resource.getId() + "!", e);
		}
	}
}
