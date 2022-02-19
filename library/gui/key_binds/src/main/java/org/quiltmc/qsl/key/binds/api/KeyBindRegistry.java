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

package org.quiltmc.qsl.key.binds.api;

import java.util.Map;

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;
import org.quiltmc.qsl.key.binds.mixin.client.KeyBindingAccessor;

/**
 * Handles the registration of modded key binds and allows for changing their properties,
 * such as their state.
 *
 * <p>This class also allows for getting key binds registered by other mods.
 */
@Environment(EnvType.CLIENT)
public class KeyBindRegistry {
	/**
	 * Registers a key bind, with it being initially enabled.
	 *
	 * <pre>
	 * {@code
	 * public static final KeyBind EXAMPLE_KEY_BIND = KeyBindRegistry.registerKeyBind(
	 * 	new KeyBind("key.modid.example", GLFW.GLFW_KEY_M, "key.modid.category")
	 * )
	 * }
	 * </pre>
	 *
	 * @param key the key bind to be registered
	 * @return the successfully-registered key bind
	 * @throws NullPointerException if {@code key} is null
	 * @throws IllegalArgumentException if either {@code key} or it's translation key is already registered
	 */
	public static KeyBind registerKeyBind(KeyBind key) {
		return registerKeyBind(key, true);
	}

	/**
	 * Registers a key bind with a specified initial state.
	 *
	 * @param key the key bind to be registered
	 * @param enabled the key bind's initial state
	 * @return the key bind if successfully registered, {@code null} otherwise
	 * @throws NullPointerException if {@code key} is null
	 * @throws IllegalArgumentException if either {@code key} or it's translation key is already registered
	 */
	public static KeyBind registerKeyBind(KeyBind key, boolean enabled) {
		return KeyBindRegistryImpl.registerKeyBind(key, enabled);
	}

	/**
	 * Searches for a modded key bind with the specified translation key in the registry.
	 *
	 * @param translationKey the key bind's translation key
	 * @return the key bind if found, {@code null} otherwise
	 */
	public static KeyBind getKeyBind(String translationKey) {
		return KeyBindRegistryImpl.getKeyBind(translationKey);
	}

	/**
	 * Gets the bound key of the key bind.
	 *
	 * <p>The bound key is only directly used by the key bind system's internal logic.
	 * If possible, use the methods provided by the KeyBind class instead.
	 *
	 * @param key the key bind
	 * @return the key bind's bound key
	 */
	public static InputUtil.Key getBoundKey(KeyBind key) {
		return ((KeyBindingAccessor) key).getBoundKey();
	}

	/**
	 * Gets the state of the key bind.
	 *
	 * <p>The state of a key bind determines whenever it's disabled or not.
	 *
	 * <p>When a key bind is disabled, it is effectively hidden from the game,
	 * being non-existent to it. options.txt is the only exception, who still
	 * stores the disabled key binds.
	 *
	 * @param key the key bind
	 * @return {@code true} if the key bind is enabled, {@code false} otherwise
	 * @throws IllegalArgumentException if {@code key} is either unregistered or a Vanilla key bind
	 */
	public static boolean isEnabled(KeyBind key) {
		return KeyBindRegistryImpl.isEnabled(key);
	}

	/**
	 * Sets the state of the key bind.
	 *
	 * <p>This method allows for disabling the key bind, making it invisible to
	 * the game. options.txt is the only exception, who still stores the disabled
	 * key binds.
	 *
	 * @param key the key bind
	 * @param enabled the new state
	 * @throws IllegalArgumentException if {@code key} is either unregistered or a Vanilla key bind
	 */
	public static void setEnabled(KeyBind key, boolean enabled) {
		KeyBindRegistryImpl.setEnabled(key, enabled);
	}

	/**
	 * Returns a map containing all modded key binds (and vanilla ones if specified).
	 *
	 * @param includeVanilla {@code true} if vanilla entries should be included, else {@code false}
	 * @return a map containing all modded (and optionally vanilla) key binds
	 */
	public static Map<KeyBind, Boolean> getAllKeyBinds(boolean includeVanilla) {
		return KeyBindRegistryImpl.getAllKeyBinds(includeVanilla);
	}
}
