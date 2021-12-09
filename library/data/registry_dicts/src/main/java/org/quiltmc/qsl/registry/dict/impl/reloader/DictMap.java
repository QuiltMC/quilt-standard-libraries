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

import static org.quiltmc.qsl.registry.dict.impl.reloader.RegistryDictReloader.LOGGER;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.Level;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;

final class DictMap {
	private final Registry<?> registry;
	private final RegistryDict<?, ?> attribute;
	private final TagGetter tagGetter;
	private final Map<DictTarget, Object> map;

	public DictMap(Registry<?> registry, RegistryDict<?, ?> attribute, TagGetter tagGetter) {
		this.registry = registry;
		this.attribute = attribute;
		this.tagGetter = tagGetter;
		map = new HashMap<>();
	}

	public void put(Identifier id, Object value) {
		map.put(new DictTarget.Single(id), value);
	}

	public void putTag(Identifier tagId, Object value) {
		map.put(new DictTarget.Tagged<>(tagGetter, registry, tagId), value);
	}

	public Registry<?> getRegistry() {
		return registry;
	}

	public RegistryDict<?, ?> getAttribute() {
		return attribute;
	}

	public Map<DictTarget, Object> getMap() {
		return map;
	}

	// FIXME tag support!
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
				handleArray(resource, values.getAsJsonArray());
			} else if (values.isJsonObject()) {
				handleObject(resource, values.getAsJsonObject());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing " + resource.getId() + "!", e);
		}
	}

	private void handleArray(Resource resource, JsonArray values) {
		for (int i = 0; i < values.size(); i++) {
			JsonElement entry = values.get(i);

			if (!entry.isJsonObject()) {
				LOGGER.error("Invalid element at index {} in values of {}: expected a JsonObject, was {}",
						i, resource.getId(), JsonHelper.getType(entry));
				continue;
			}
			JsonObject entryO = entry.getAsJsonObject();
			Identifier id;
			boolean tagId = false;
			JsonElement value;
			boolean required;

			try {
				String idStr;
				if (entryO.has("id")) {
					idStr = JsonHelper.getString(entryO, "id");
				} else if (entryO.has("tag")) {
					tagId = true;
					idStr = JsonHelper.getString(entryO, "tag");
				} else {
					throw new JsonSyntaxException("Expected id or tag, got neither");
				}
				id = new Identifier(idStr);
			} catch (JsonSyntaxException e) {
				LOGGER.error("Invalid element at index {} in values of {}: syntax error",
						i, resource.getId());
				LOGGER.catching(Level.ERROR, e);
				continue;
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid element at index {} in values of {}: invalid identifier",
						i, resource.getId());
				LOGGER.catching(Level.ERROR, e);
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
				continue;
			}

			required = JsonHelper.getBoolean(entryO, "required", true);

			if (!tagId && required && !registry.containsId(id)) {
				LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resource.getId(), id);
				continue;
			}

			Object parsedValue = parseValue(resource, id, value);
			if (parsedValue == null) {
				continue;
			}

			if (tagId) {
				putTag(id, parsedValue);
			} else {
				put(id, parsedValue);
			}
		}
	}

	private void handleObject(Resource resource, JsonObject values) {
		for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
			Identifier id;
			boolean tagId = false;
			try {
				String idStr = entry.getKey();
				if (idStr.startsWith("#")) {
					tagId = true;
					idStr = idStr.substring(1);
				}
				id = new Identifier(idStr);
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid identifier in values of {}: '{}', ignoring",
						resource.getId(), entry.getKey());
				LOGGER.catching(Level.ERROR, e);
				continue;
			}

			if (!tagId && !registry.containsId(id)) {
				LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resource.getId(), id);
				continue;
			}

			Object parsedValue = parseValue(resource, id, entry.getValue());
			if (parsedValue == null) {
				continue;
			}

			if (tagId) {
				putTag(id, parsedValue);
			} else {
				put(id, parsedValue);
			}
		}
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
}
