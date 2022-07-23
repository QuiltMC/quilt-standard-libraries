/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.key.binds.impl.config;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;
import org.quiltmc.loader.api.config.QuiltConfig;
import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;

@SuppressWarnings("unchecked")
public class QuiltKeyBindsConfigManager {
	public static final QuiltKeyBindsConfig CONFIG = QuiltConfig.create("qsl", "key_binds", QuiltKeyBindsConfig.class);

	public static final TrackedValue<Boolean> SHOW_TUTORIAL_TOAST = (TrackedValue<Boolean>) CONFIG.getValue(List.of("show_tutorial_toast"));
	public static final TrackedValue<ValueMap<ValueList<String>>> KEY_BINDS = (TrackedValue<ValueMap<ValueList<String>>>) CONFIG.getValue(List.of("key_binds"));
	public static final TrackedValue<ValueMap<ValueList<String>>> UNUSED_KEY_BINDS = (TrackedValue<ValueMap<ValueList<String>>>) CONFIG.getValue(List.of("unused_key_binds"));

	public QuiltKeyBindsConfigManager() { }

	public static void updateConfig(boolean load) {
		var filteredList = new ArrayList<>(KeyBindRegistryImpl.getAllKeyBinds());
		List<String> removalList = new ArrayList<>();

		KEY_BINDS.value().putAll(UNUSED_KEY_BINDS.value());
		UNUSED_KEY_BINDS.value().clear();

		for (String keyBindKey : KEY_BINDS.value().keySet()) {
			System.out.println(keyBindKey);
			var keyBind = KeyBindRegistryImpl.getKeyBind(keyBindKey);
			var keyList = KEY_BINDS.value().get(keyBindKey);
			if (keyBind != null && keyBind.isEnabled()) {
				if (load) {
					switch (keyList.size()) {
						case 0 -> keyBind.setBoundKey(InputUtil.UNKNOWN_KEY);
						case 1 -> keyBind.setBoundKey(InputUtil.fromTranslationKey(keyList.get(0)));
						default -> {
							SortedMap<InputUtil.Key, Boolean> map = new TreeMap<>();
							for (String string : keyList) {
								map.put(InputUtil.fromTranslationKey(string), false);
							}

							keyBind.setBoundChord(new KeyChord(map));
						}
					};
				} else {
					ValueList<String> list;
					if (keyBind.getBoundChord() == null) {
						if (keyBind.getBoundKey().equals(InputUtil.UNKNOWN_KEY)) {
							list = ValueList.create("", new String[] {});
						} else {
							list = ValueList.create("", keyBind.getKeyTranslationKey());
						}
					} else {
						var protoList = keyBind.getBoundChord().keys.keySet().stream().map(key -> key.getTranslationKey()).toList();
						var array = protoList.toArray(new String[] {});
						list = ValueList.create("", array);
					}

					keyList.clear();
					keyList.addAll(list);
				}

				filteredList.remove(keyBind);
			} else {
				removalList.add(keyBindKey);
				UNUSED_KEY_BINDS.value().put(keyBindKey, keyList);
			}
		}

		removalList.forEach(key -> KEY_BINDS.value().remove(key));

		for (KeyBind keyBind : filteredList) {
			if (keyBind.isEnabled()) {
				ValueList<String> list;
				if (keyBind.getBoundChord() == null) {
					if (keyBind.getBoundKey().equals(InputUtil.UNKNOWN_KEY)) {
						list = ValueList.create("", new String[] {});
					} else {
						list = ValueList.create("", keyBind.getKeyTranslationKey());
					}
				} else {
					var protoList = keyBind.getBoundChord().keys.keySet().stream().map(key -> key.getTranslationKey()).toList();
					var array = protoList.toArray(new String[] {});
					list = ValueList.create("", array);
				}

				KEY_BINDS.value().put(keyBind.getTranslationKey(), list);
			}
		}
	}
}
