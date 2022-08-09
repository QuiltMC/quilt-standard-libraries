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

package org.quiltmc.qsl.registry.attachment.impl.reloader;

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

import static org.quiltmc.qsl.registry.attachment.impl.reloader.RegistryEntryAttachmentReloader.LOGGER;

final class AttachmentDictionary<R, V> {
	private final Registry<R> registry;
	private final RegistryEntryAttachment<R, V> attachment;
	private final Map<ValueTarget, Object> map;

	public AttachmentDictionary(Registry<R> registry, RegistryEntryAttachment<R, V> attachment) {
		this.registry = registry;
		this.attachment = attachment;
		this.map = new HashMap<>();
	}

	public void put(Identifier id, Object value) {
		this.map.put(new ValueTarget(id, ValueTarget.Type.ENTRY), value);
	}

	public void putTag(Identifier id, Object value) {
		this.map.put(new ValueTarget(id, ValueTarget.Type.TAG), value);
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

	public void processResource(Resource resource) {
		try {
			boolean replace;
			JsonElement values;

			try (var reader = new InputStreamReader(resource.open())) {
				JsonObject obj = JsonHelper.deserialize(reader);
				replace = JsonHelper.getBoolean(obj, "replace", false);
				values = obj.get("values");

				if (values == null) {
					throw new JsonSyntaxException("Missing values, expected to find an array or object");
				} else if (!values.isJsonArray() && !values.isJsonObject()) {
					throw new JsonSyntaxException("Expected values to be an or object," +
							"was " + JsonHelper.getType(values));
				}
			} catch (JsonSyntaxException e) {
				LOGGER.error("Invalid JSON file '" + resource.getSourceName() + "', ignoring", e);
				return;
			}

			// if "replace" is true, the data file wants us to clear all entries from other files before it
			if (replace) {
				this.map.clear();
			}

			if (values.isJsonArray()) {
				this.handleArray(resource, values.getAsJsonArray());
			} else if (values.isJsonObject()) {
				this.handleObject(resource, values.getAsJsonObject());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing '" + resource.getSourceName() + "'!", e);
		}
	}

	private void handleArray(Resource resource, JsonArray values) {
		for (int i = 0; i < values.size(); i++) {
			JsonElement entry = values.get(i);

			if (!entry.isJsonObject()) {
				LOGGER.error("Invalid element at index {} in values of '{}': expected an object, was {}",
						i, resource.getSourceName(), JsonHelper.getType(entry));
				continue;
			}

			JsonObject entryO = entry.getAsJsonObject();
			Identifier id;
			boolean isTag = false;
			JsonElement value;
			boolean required;

			try {
				String idStr;

				if (entryO.has("id")) {
					idStr = JsonHelper.getString(entryO, "id");
				} else if (entryO.has("tag")) {
					isTag = true;
					idStr = JsonHelper.getString(entryO, "tag");
				} else {
					throw new JsonSyntaxException("Expected id or tag, got neither");
				}

				id = new Identifier(idStr);
			} catch (JsonSyntaxException e) {
				LOGGER.error("Invalid element at index {} in values of '{}': syntax error",
						i, resource.getSourceName());
				LOGGER.error("", e);
				continue;
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid element at index {} in values of '{}': invalid identifier",
						i, resource.getSourceName());
				LOGGER.error("", e);
				continue;
			}

			try {
				value = entryO.get("value");
				if (value == null) {
					throw new JsonSyntaxException("Missing value");
				}
			} catch (JsonSyntaxException e) {
				LOGGER.error("Failed to parse value for registry entry {} in values of '{}': syntax error",
						id, resource.getSourceName());
				LOGGER.error("", e);
				continue;
			}

			required = JsonHelper.getBoolean(entryO, "required", true);

			handleEntry(resource, id, isTag, required, value);
		}
	}

	private void handleObject(Resource resource, JsonObject values) {
		for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
			Identifier id;
			boolean isTag = false;
			boolean required = true;

			try {
				String idStr = entry.getKey();

				if (idStr.startsWith("#")) {
					isTag = true;
					idStr = idStr.substring(1);
				}

				id = new Identifier(idStr);
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid identifier in values of '{}': '{}', ignoring",
						resource.getSourceName(), entry.getKey());
				LOGGER.error("", e);
				continue;
			}

			handleEntry(resource, id, isTag, required, entry.getValue());
		}
	}

	private void handleEntry(Resource resource, Identifier id, boolean isTag, boolean required, JsonElement value) {
		if (!isTag && !registry.containsId(id)) {
			if (required) {
				// log an error
				// vanilla tags throw but that causes way more breakage
				LOGGER.error("Unregistered identifier in values of '{}': '{}', ignoring", resource.getSourceName(), id);
			}
			// either way, drop the entry
			return;
		}

		DataResult<?> parseResult = this.attachment.codec().parse(JsonOps.INSTANCE, value);

		if (parseResult.result().isEmpty()) {
			if (parseResult.error().isPresent()) {
				LOGGER.error("Failed to parse value for attachment {} of registry entry {}: {}",
						this.attachment.id(), id, parseResult.error().get().message());
			} else {
				LOGGER.error("Failed to parse value for attachment {} of registry entry {}: unknown error",
						this.attachment.id(), id);
			}

			LOGGER.error("Ignoring attachment value for {} in '{}' since it's invalid", id, resource.getSourceName());
			return;
		}

		Object parsedValue = parseResult.result().get();
		if (isTag) {
			this.putTag(id, parsedValue);
		} else {
			this.put(id, parsedValue);
		}
	}

	public record ValueTarget(Identifier id, Type type) {
		enum Type {
			ENTRY, TAG;
		}
	}
}
