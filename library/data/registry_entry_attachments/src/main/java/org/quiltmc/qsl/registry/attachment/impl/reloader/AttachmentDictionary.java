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
import java.util.Map;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

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
	private final Map<Identifier, Identifier> mirrors, tagMirrors;

	public AttachmentDictionary(Registry<R> registry, RegistryEntryAttachment<R, V> attachment) {
		this.registry = registry;
		this.attachment = attachment;
		this.map = new Object2ReferenceOpenHashMap<>();
		this.mirrors = new Object2ObjectOpenHashMap<>();
		this.tagMirrors = new Object2ObjectOpenHashMap<>();
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

	public Map<Identifier, Identifier> getMirrors() {
		return mirrors;
	}

	public Map<Identifier, Identifier> getTagMirrors() {
		return tagMirrors;
	}

	public void processResource(Identifier resourceId, Resource resource) {
		try {
			boolean replace;
			JsonElement values;
			boolean replaceMirrors;
			JsonObject mirrors;
			boolean replaceTagMirrors;
			JsonObject tagMirrors;

			try (var reader = new InputStreamReader(resource.open())) {
				JsonObject obj = JsonHelper.deserialize(reader);
				replace = JsonHelper.getBoolean(obj, "replace", false);
				values = obj.get("values");

				if (values == null) {
					throw new JsonSyntaxException("Missing values, expected to find a JsonArray or JsonObject");
				} else if (!values.isJsonArray() && !values.isJsonObject()) {
					throw new JsonSyntaxException("Expected values to be a JsonArray or JsonObject," +
							"was " + JsonHelper.getType(values));
				}

				replaceMirrors = JsonHelper.getBoolean(obj, "replace_mirrors", false);
				mirrors = JsonHelper.getObject(obj, "mirrors", null);
				replaceTagMirrors = JsonHelper.getBoolean(obj, "replace_tag_mirrors", false);
				tagMirrors = JsonHelper.getObject(obj, "tag_mirrors", null);
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

			if (mirrors != null) {
				if (replaceMirrors) {
					this.mirrors.clear();
				}

				this.handleMirrors(this.mirrors, resourceId, mirrors, true);
			}

			if (tagMirrors != null) {
				if (replaceTagMirrors) {
					this.tagMirrors.clear();
				}

				this.handleMirrors(this.tagMirrors, resourceId, tagMirrors, false);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing " + resourceId + "!", e);
		}
	}

	private void handleArray(Identifier resourceId, Resource resource, JsonArray values) {
		for (int i = 0; i < values.size(); i++) {
			JsonElement entry = values.get(i);

			if (!entry.isJsonObject()) {
				LOGGER.error("Invalid element at index {} in values of {}: expected an object, was {}",
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

			if (!tagId && required && !this.registry.containsId(id)) {
				LOGGER.error("Unregistered identifier in values of {}: '{}', ignoring", resourceId, id);
				continue;
			}

			Object parsedValue = this.parseValue(resource, id, value);
			if (parsedValue == null) {
				continue;
			}

			if (tagId) {
				this.putTag(id, parsedValue);
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
				this.putTag(id, parsedValue);
			} else {
				this.put(id, parsedValue);
			}
		}
	}

	private void handleMirrors(Map<Identifier, Identifier> map, Identifier resourceId, JsonObject mirrors,
			boolean checkRegistry) {
		for (Map.Entry<String, JsonElement> entry : mirrors.entrySet()) {
			Identifier target;
			try {
				target = new Identifier(entry.getKey());
			} catch (InvalidIdentifierException e) {
				LOGGER.error("Invalid identifier in mirrors of {}: '{}', ignoring",
						resourceId, entry.getKey());
				LOGGER.error("", e);
				continue;
			}

			if (checkRegistry && !this.registry.containsId(target)) {
				continue;
			}

			if (entry.getValue() instanceof JsonPrimitive prim && prim.isString()) {
				Identifier source;
				try {
					source = new Identifier(prim.getAsString());
				} catch (InvalidIdentifierException e) {
					LOGGER.error("Invalid mirror '{}' in {}: invalid source identifier, ignoring",
							target, resourceId);
					LOGGER.error("", e);
					continue;
				}

				if (checkRegistry && !this.registry.containsId(source)) {
					continue;
				}

				map.put(target, source);
			} else {
				LOGGER.error("Invalid mirror '{}' in {}: expected string, got {}; ignoring",
						target, resourceId, JsonHelper.getType(entry.getValue()));
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

			LOGGER.error("Ignoring attachment value for '{}' in {} since it's invalid", id, resource.getSourceName());
			return null;
		}

		return parsedValue.result().get();
	}

	public record ValueTarget(Identifier id, Type type) {
		enum Type {
			ENTRY, TAG;
		}
	}
}
