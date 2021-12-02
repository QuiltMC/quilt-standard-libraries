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

package org.quiltmc.qsl.key.bindings.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.mixin.client.KeyBindingAccessor;

public class KeyBindingRegistryImpl {
	private static Map<KeyBinding, Boolean> quiltKeys = new HashMap<>();
	private static List<KeyBindingManager> keyBindingManagers = new ArrayList<>(1);
	private static KeyBinding[] enabledQuiltKeysArray = new KeyBinding[] {};
	private static List<KeyBinding> disabledQuiltKeys = new ArrayList<>();

	// TODO - Currently, two different key binds with the same key can be registered. This is not good
	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		quiltKeys.put(key, enabled);
		applyChanges(true);
		if (!enabled) {
			KeyBindingAccessor.getKeysById().remove(key.getTranslationKey());
		}

		return key;
	}

	public static boolean getEnabled(KeyBinding key) {
		if (quiltKeys.containsKey(key)) {
			return quiltKeys.get(key);
		}

		return false;
	}

	public static void setEnabled(KeyBinding key, boolean newEnabled) {
		if (quiltKeys.containsKey(key)) {
			quiltKeys.replace(key, newEnabled);
			applyChanges(false);
			if (newEnabled) {
				KeyBindingAccessor.getKeysById().put(key.getTranslationKey(), key);
			} else {
				KeyBindingAccessor.getKeysById().remove(key.getTranslationKey(), key);
			}

			((KeyBindingAccessor) key).callReset();
			KeyBinding.updateKeysByCode();
		}
	}

	public static void registerKeyBindingManager(GameOptions options, KeyBinding[] allKeys) {
		keyBindingManagers.add(new KeyBindingManager(options, allKeys));
	}

	public static void updateKeysArray() {
		List<KeyBinding> enabledQuiltKeys = new ArrayList<>();
		disabledQuiltKeys.clear();
		for (var entry : quiltKeys.entrySet()) {
			if (entry.getValue()) {
				enabledQuiltKeys.add(entry.getKey());
			} else {
				disabledQuiltKeys.add(entry.getKey());
			}
		}

		KeyBinding[] quiltKeysArray = enabledQuiltKeys.toArray(new KeyBinding[enabledQuiltKeys.size()]);

		enabledQuiltKeysArray = quiltKeysArray;
	}

	public static KeyBinding[] getKeyBindings(KeyBinding[] allVanillaKeys) {
		return ArrayUtils.addAll(allVanillaKeys, enabledQuiltKeysArray);
	}

	public static List<KeyBinding> getDisabledKeyBindings() {
		return disabledQuiltKeys;
	}

	public static void applyChanges(boolean updateTotal) {
		updateKeysArray();
		for (KeyBindingManager manager : keyBindingManagers) {
			manager.addModdedKeyBinds();
		}
	}
}
