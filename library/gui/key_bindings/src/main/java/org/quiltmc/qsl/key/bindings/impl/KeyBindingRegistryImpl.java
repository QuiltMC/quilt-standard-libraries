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
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.mixin.client.KeyBindingAccessor;

@Environment(EnvType.CLIENT)
public class KeyBindingRegistryImpl {
	public static final Logger LOGGER = LogManager.getLogger();

	private static Map<KeyBinding, Boolean> quiltKeys = new HashMap<>();
	private static KeyBinding[] enabledQuiltKeysArray = new KeyBinding[] {};
	private static List<KeyBinding> disabledQuiltKeys = new ArrayList<>(0);
	private static KeyBindingManager keyBindingManager = null;

	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		Objects.requireNonNull(key, "Attempted to register a null key bind!");

		for (KeyBinding otherKey : quiltKeys.keySet()) {
			if (key == otherKey) {
				throw new IllegalArgumentException(String.format("%s has already been registered!", key.getTranslationKey()));
			} else if (key.getTranslationKey().equals(otherKey.getTranslationKey())) {
				throw new IllegalArgumentException(String.format("Attempted to register {}, but a key bind with the same translation key has already been registered!", key.getTranslationKey()));
			}
		}

		quiltKeys.put(key, enabled);
		applyChanges();
		if (!enabled) {
			KeyBindingAccessor.getKeysById().remove(key.getTranslationKey());
		}

		return key;
	}

	public static KeyBinding getKeyBinding(String translationKey) {
		for (KeyBinding key : quiltKeys.keySet()) {
			if (key.getTranslationKey().equals(translationKey)) {
				return key;
			}
		}

		return null;
	}

	public static boolean isEnabled(KeyBinding key) {
		if (quiltKeys.containsKey(key)) {
			return quiltKeys.get(key);
		}

		// TODO - Split those two
		throw new IllegalArgumentException(String.format("%s has either not been registered or it is a vanilla key!", key.getTranslationKey()));
	}

	public static boolean setEnabled(KeyBinding key, boolean enabled) {
		if (quiltKeys.containsKey(key)) {
			quiltKeys.replace(key, enabled);
			applyChanges();
			if (enabled) {
				KeyBindingAccessor.getKeysById().put(key.getTranslationKey(), key);
			} else {
				KeyBindingAccessor.getKeysById().remove(key.getTranslationKey(), key);
			}

			((KeyBindingAccessor) key).callReset();
			KeyBinding.updateKeysByCode();

			return true;
		}

		return false;
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

		enabledQuiltKeysArray = enabledQuiltKeys.toArray(new KeyBinding[enabledQuiltKeys.size()]);
	}

	public static KeyBinding[] getKeyBindings() {
		return ArrayUtils.addAll(keyBindingManager.getAllKeys(), enabledQuiltKeysArray);
	}

	public static List<KeyBinding> getDisabledKeyBindings() {
		return disabledQuiltKeys;
	}

	public static void setKeyBindingManager(KeyBindingManager manager) {
		keyBindingManager = manager;
	}

	public static void applyChanges() {
		updateKeysArray();
		if (keyBindingManager != null) {
			keyBindingManager.addModdedKeyBinds();
		}
	}
}
