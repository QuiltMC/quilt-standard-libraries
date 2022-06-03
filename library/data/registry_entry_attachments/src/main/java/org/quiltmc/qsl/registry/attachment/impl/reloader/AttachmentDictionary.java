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

package org.quiltmc.qsl.registry.attachment.impl.reloader;

import static org.quiltmc.qsl.registry.attachment.impl.reloader.RegistryEntryAttachmentReloader.LOGGER;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

final class AttachmentDictionary<R, V> {
	private final Registry<R> registry;
	private final RegistryEntryAttachment<R, V> attachment;
	private final boolean isClient;
	private final Map<ValueTarget, Object> map;

	public AttachmentDictionary(Registry<R> registry, RegistryEntryAttachment<R, V> attachment, boolean isClient) {
		this.registry = registry;
		this.attachment = attachment;
		this.isClient = isClient;
		this.map = new HashMap<>();
	}

	public void put(Identifier id, Object value) {
		this.map.put(new ValueTarget.Single(id), value);
	}

	public void putTag(Identifier id, Object value, boolean required) {
		this.map.put(new ValueTarget.Tagged<>(this.registry, id, this.isClient, required), value);
	}

	public Registry<?> getRegistry() {
		return this.registry;
	}

	public RegistryEntryAttachment<?, ?> getAttachment() {
		return this.attachment;
	}

	public Map<ValueTarget, Object> getMap() {
		return this.map;
	}

	public void processResource(Identifier resourceId, Resource resource) {
		try {
			boolean replace;
			JsonElement values;

			try {
				JsonObject obj = JsonHelper.deserialize(new InputStreamReader(resource.method_14482()));
				replace = JsonHelper.getBoolean(obj, "replace", false);
				values = obj.get("values");

				if (values == null) {
					throw new JsonSyntaxException("Missing values, expected to find a JsonArray or JsonObject");
				} else if (!values.isJsonArray() && !values.isJsonObject()) {
					throw new JsonSyntaxException("Expected values to be a JsonArray or JsonObject," +
							"was " + JsonHelper.getType(values));
				}
			} catch (JsonSyntaxException e) {
				LOGGER.error("Invalid JSON file " + resourceId + ", ignoring", e);
				return;
			}

			// if "replace" is true, the data file wants us to clear all entries from other files before it
			if (replace) {
				this.map.clear();
			}

			if (values.isJsonArray()) {
				this.handleArray(resourceId, resource, values.getAsJsonArray());
			} else if (values.isJsonObject()) {
				this.handleObject(resourceId, resource, values.getAsJsonObject());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing " + resourceId + "!", e);
		}
	}

	private void handleArray(Identifier resourceId, Resource resource, JsonArray values) {
		for (int i = 0; i < values.size(); i++) {
			JsonElement entry = values.get(i);

			if (!entry.isJsonObject()) {
				LOGGER.error("Invalid element at index {} in values of {}: expected a JsonObject, was {}",
						i, resourceId, JsonHelper.getType(entry));
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
						i, resourceId);
				LOGGER.error("", e);
				continue;
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid element at index {} in values of {}: invalid identifier",
						i, resourceId);
				LOGGER.error("", e);
				continue;
			}

			try {
				value = entryO.get("value");
				if (value == null) {
					throw new JsonSyntaxException("Missing value");
				}
			} catch (JsonSyntaxException e) {
				LOGGER.error("Failed to parse value for registry entry {} in values of {}: syntax error",
						id, resourceId);
				LOGGER.error("", e);
				continue;
			}

			required = JsonHelper.getBoolean(entryO, "required", true);

			if (!tagId && required && !registry.containsId(id)) {
				LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resourceId, id);
				continue;
			}

			Object parsedValue = this.parseValue(resource, id, value);
			if (parsedValue == null) {
				continue;
			}

			if (tagId) {
				this.putTag(id, parsedValue, required);
			} else {
				this.put(id, parsedValue);
			}
		}
	}

	private void handleObject(Identifier resourceId, Resource resource, JsonObject values) {
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
						resourceId, entry.getKey());
				LOGGER.error("", e);
				continue;
			}

			if (!tagId && !registry.containsId(id)) {
				LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resourceId, id);
				continue;
			}

			Object parsedValue = this.parseValue(resource, id, entry.getValue());
			if (parsedValue == null) {
				continue;
			}

			if (tagId) {
				this.putTag(id, parsedValue, false);
			} else {
				this.put(id, parsedValue);
			}
		}
	}

	private Object parseValue(Resource resource, Identifier id, JsonElement value) {
		DataResult<?> parsedValue = this.attachment.codec().parse(JsonOps.INSTANCE, value);

		if (parsedValue.result().isEmpty()) {
			if (parsedValue.error().isPresent()) {
				LOGGER.error("Failed to parse value for attachment {} of registry entry {}: {}",
						this.attachment.id(), id, parsedValue.error().get().message());
			} else {
				LOGGER.error("Failed to parse value for attachment {} of registry entry {}: unknown error",
						this.attachment.id(), id);
			}

			LOGGER.error("Ignoring attachment value for '{}' in {} since it's invalid", id, resource.method_14480());
			return null;
		}

		return parsedValue.result().get();
	}
}
