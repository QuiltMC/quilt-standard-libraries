/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync.mod_protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;

import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.registry.impl.RegistryConfig;

public class ModProtocolImpl {
	public static boolean enabled = false;
	public static boolean disableQuery = false;
	public static String prioritizedId = "";
	public static ModProtocolDef prioritizedEntry;
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Map<String, ModProtocolDef> PROTOCOL_VERSIONS = new HashMap<>();
	public static final List<ModProtocolDef> REQUIRED = new ArrayList<>();
	public static final List<ModProtocolDef> ALL = new ArrayList<>();

	@SuppressWarnings("ConstantConditions")
	public static void loadVersions() {
		//var modProto = RegistryConfig.INSTANCE.registry_sync;
		//disableQuery = modProto.disable_mod_protocol_ping;
		disableQuery = (Boolean) RegistryConfig.getSync("disable_mod_protocol_ping");

		//if (modProto.mod_protocol_version >= 0) {
		if (((Number) RegistryConfig.getSync("mod_protocol_version")).intValue() >= 0) {
			//prioritizedEntry = new ModProtocolDef("global:" + modProto.mod_protocol_id, modProto.mod_protocol_name, IntList.of(modProto.mod_protocol_version), false);
			prioritizedEntry = new ModProtocolDef("global:" + (String) RegistryConfig.getSync("mod_protocol_id"), (String) RegistryConfig.getSync("mod_protocol_name"), IntList.of(((Number) RegistryConfig.getSync("mod_protocol_version")).intValue()), false);
			prioritizedId = prioritizedEntry.id();
			add(prioritizedEntry);
		}

		for (var container : QuiltLoader.getAllMods()) {
			var data = container.metadata();
			var quiltRegistry = data.value("quilt_registry");

			if (quiltRegistry == null) {
				continue;
			}

			if (quiltRegistry.type() != LoaderValue.LType.OBJECT) {
				LOGGER.warn("Mod {} ({}) contains invalid 'quilt_registry' entry! Expected 'OBJECT', found '{}'", container.metadata().name(), container.metadata().id(), quiltRegistry.type());
				continue;
			}

			var value = quiltRegistry.asObject().get("mod_protocol");

			if (value == null || value.type() == LoaderValue.LType.NULL) {
				continue;
			}

			if (value.type() == LoaderValue.LType.OBJECT) {
				var object = value.asObject();

				var optional = false;
				var optVal = object.get("optional");

				if (optVal != null) {
					if (optVal.type() != LoaderValue.LType.BOOLEAN) {
						invalidEntryType(".optional", container, LoaderValue.LType.BOOLEAN, optVal.type());
						continue;
					}

					optional = optVal.asBoolean();
				}

				var version = decodeVersion(".value", container, object.get("value"));

				if (version != null) {
					add(new ModProtocolDef("mod:" + data.id(), data.name(), version, optional));
				}
			} else {
				var version = decodeVersion("", container, value);
				if (version != null) {
					add(new ModProtocolDef("mod:" + data.id(), data.name(), version, false));
				}
			}
		}
	}

	private static IntList decodeVersion(String path, ModContainer container, LoaderValue value) {
		if (value == null) {
			invalidEntryType(path, container, LoaderValue.LType.NUMBER, LoaderValue.LType.NULL);
			return null;
		} else if (value.type() == LoaderValue.LType.NUMBER) {
			var i = value.asNumber().intValue();
			if (i < 0) {
				negativeEntry(path, container, i);
				return null;
			}

			return IntList.of(i);
		} else if (value.type() == LoaderValue.LType.ARRAY) {
			var array = value.asArray();
			var versions = new IntArrayList(array.size());
			for (var i = 0; i < array.size(); i++) {
				var entry = array.get(i);
				if (entry.type() == LoaderValue.LType.NUMBER) {
					var version = entry.asNumber().intValue();
					if (version < 0) {
						negativeEntry(path + "[" + i + "]", container, version);
						return null;
					}

					versions.add(version);
				} else {
					invalidEntryType(path + "[" + i + "]", container, LoaderValue.LType.NUMBER, entry.type());
					return null;
				}
			}

			return versions;
		} else {
			invalidEntryType(path + ".optional", container, LoaderValue.LType.NUMBER, value.type());
			return null;
		}
	}

	private static void invalidEntryType(String path, ModContainer c, LoaderValue.LType expected, LoaderValue.LType found) {
		LOGGER.warn("Mod {} ({}) contains invalid 'quilt_registry.mod_protocol{}' entry! Expected '{}', found '{}'", path, c.metadata().name(), c.metadata().id(), expected.name(), found.name());
	}

	private static void negativeEntry(String path, ModContainer c, int i) {
		LOGGER.warn("Mod {} ({}) contains invalid 'quilt_registry.mod_protocol{}' entry! Protocol requires non-negative integer, found '{}'!", path, c.metadata().name(), c.metadata().id(), i);
	}

	public static IntList getVersion(String string) {
		var x = PROTOCOL_VERSIONS.get(string);
		return x == null ? IntList.of() : x.versions();
	}

	public static void add(ModProtocolDef def) {
		PROTOCOL_VERSIONS.put(def.id(), def);

		if (!def.optional()) {
			REQUIRED.add(def);
		}

		ALL.add(def);
		enabled = true;
	}
}
