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
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class KeyBindRegistryImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("KeyBindRegistry");

	// TODO - FastUtils
	private static final List<KeyBind> QUILT_KEY_BINDS = new ArrayList<>();
	private static final List<KeyBind> DISABLED_KEYS = new ArrayList<>(0);
	private static KeyBind[] enabledQuiltKeysArray = new KeyBind[] {};
	private static KeyBindManager keyBindManager = null;

	public static KeyBind registerKeyBind(KeyBind key) {
		Objects.requireNonNull(key, "Attempted to register a null key bind!");

		for (KeyBind otherKey : QUILT_KEY_BINDS) {
			if (key.equals(otherKey)) {
				throw new IllegalArgumentException(String.format("%s has already been registered!", key.getTranslationKey()));
			} else if (key.getTranslationKey().equals(otherKey.getTranslationKey())) {
				throw new IllegalArgumentException(String.format("Attempted to register {}, but a key bind with the same translation key has already been registered!", key.getTranslationKey()));
			}
		}

		QUILT_KEY_BINDS.add(key);
		applyChanges();

		return key;
	}

	public static KeyBind getKeyBind(String translationKey) {
		for (KeyBind key : QUILT_KEY_BINDS) {
			if (key.getTranslationKey().equals(translationKey)) {
				return key;
			}
		}

		return null;
	}

	public static boolean throwUnregisteredKeyException(KeyBind key) {
		if (keyBindManager != null && Arrays.asList(keyBindManager.getAllKeys()).contains(key)) {
			throw new IllegalArgumentException(String.format("%s is a vanilla key and therefore doesn't have an active state!", key.getTranslationKey()));
		}

		throw new IllegalArgumentException(String.format("%s isn't a registered key!", key.getTranslationKey()));
	}

	// TODO - includeVanilla is bad; Let's change it to something else
	public static List<KeyBind> getAllKeyBinds(boolean includeVanilla) {
		List<KeyBind> allKeys = new ArrayList<>();

		if (includeVanilla) {
			for (int i = 0; i < keyBindManager.getAllKeys().length; i++) {
				allKeys.add(keyBindManager.getAllKeys()[i]);
			}
		}

		allKeys.addAll(QUILT_KEY_BINDS);

		return allKeys;
	}

	// TODO - Wait a hecking minute, this isn't applying the changes as intended!
	public static void applyChanges() {
		List<KeyBind> enabledKeys = new ArrayList<>();
		DISABLED_KEYS.clear();
		for (KeyBind key : QUILT_KEY_BINDS) {
			(key.isEnabled() ? enabledKeys : DISABLED_KEYS).add(key);
		}

		enabledQuiltKeysArray = enabledKeys.toArray(new KeyBind[enabledKeys.size()]);

		if (keyBindManager != null) {
			keyBindManager.addModdedKeyBinds();
		}

		QuiltKeyBindsConfigManager.saveConfig();
	}

	public static KeyBind[] getKeyBinds() {
		return ArrayUtils.addAll(keyBindManager.getAllKeys(), enabledQuiltKeysArray);
	}

	public static List<KeyBind> getDisabledKeyBinds() {
		return DISABLED_KEYS;
	}

	public static void setKeyBindManager(KeyBindManager manager) {
		keyBindManager = manager;
	}
}
