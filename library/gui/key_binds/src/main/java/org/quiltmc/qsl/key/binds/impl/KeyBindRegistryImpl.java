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

package org.quiltmc.qsl.key.binds.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.binds.mixin.client.KeyBindingAccessor;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class KeyBindRegistryImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("KeyBindRegistry");

	private static Map<KeyBinding, Boolean> quiltKeys = new HashMap<>();
	private static KeyBinding[] enabledQuiltKeysArray = new KeyBinding[] {};
	private static List<KeyBinding> disabledQuiltKeys = new ArrayList<>(0);
	private static KeyBindManager keyBindManager = null;

	public static KeyBinding registerKeyBind(KeyBinding key, boolean enabled) {
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

	public static KeyBinding getKeyBind(String translationKey) {
		for (KeyBinding key : quiltKeys.keySet()) {
			if (key.getTranslationKey().equals(translationKey)) {
				return key;
			}
		}

		return null;
	}

	public static boolean throwUnregisteredKeyException(KeyBinding key) {
		if (keyBindManager != null && Arrays.asList(keyBindManager.getAllKeys()).contains(key)) {
			throw new IllegalArgumentException(String.format("%s is a vanilla key and therefore doesn't have an active state!", key.getTranslationKey()));
		}

		throw new IllegalArgumentException(String.format("%s isn't a registered key!", key.getTranslationKey()));
	}

	public static boolean isEnabled(KeyBinding key) {
		if (quiltKeys.containsKey(key)) {
			return quiltKeys.get(key);
		} else {
			return throwUnregisteredKeyException(key);
		}
	}

	public static void setEnabled(KeyBinding key, boolean enabled) {
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
		} else {
			throwUnregisteredKeyException(key);
		}
	}

	public static Map<KeyBinding, Boolean> getAllKeyBinds(boolean includeVanilla) {
		Map<KeyBinding, Boolean> allKeys = new HashMap<>();

		if (includeVanilla) {
			for (int i = 0; i < keyBindManager.getAllKeys().length; i++) {
				allKeys.put(keyBindManager.getAllKeys()[i], false);
			}
		}

		allKeys.putAll(quiltKeys);

		return allKeys;
	}

	public static void applyChanges() {
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

		if (keyBindManager != null) {
			keyBindManager.addModdedKeyBinds();
		}
	}

	public static KeyBinding[] getKeyBinds() {
		return ArrayUtils.addAll(keyBindManager.getAllKeys(), enabledQuiltKeysArray);
	}

	public static List<KeyBinding> getDisabledKeyBinds() {
		return disabledQuiltKeys;
	}

	public static void setKeyBindManager(KeyBindManager manager) {
		keyBindManager = manager;
	}
}
