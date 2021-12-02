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

package org.quiltmc.qsl.key.bindings.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;

/**
 * Handles the registration of modded key binds and allows for changing their properties,
 * such as their state.
 *
 * <p>This class also allows for getting key binds registered by other mods.
 */
@Environment(EnvType.CLIENT)
public class KeyBindingRegistry {
	/**
	 * Registers a key binding with it being initially enabled.
	 *
	 * @param key the key binding to be registered
	 * @return the key binding if successfully registered, else null
	 */
	public static KeyBinding registerKeyBinding(KeyBinding key) {
		return registerKeyBinding(key, true);
	}

	/**
	 * Registers a key binding with the specified initial state.
	 *
	 * @param key the key binding to be registered
	 * @param enabled the key binding's initial state
	 * @return the key binding if successfully registered, {@code null} otherwise
	 */
	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		return KeyBindingRegistryImpl.registerKeyBinding(key, enabled);
	}

	/**
	 * Searches for a modded key binding with the specified translation key in the registry.
	 *
	 * @param translationKey the translation key of the key binding
	 * @return the key binding if it was found, {@code null} otherwise
	 */
	public static KeyBinding getKeyBinding(String translationKey) {
		return KeyBindingRegistryImpl.getKeyBinding(translationKey);
	}

	/**
	 * Gets the state of the key binding.
	 *
	 * <p>The state of a key binding determines whenever it's disabled or not.
	 *
	 * <p>When a keybind is disabled, it is effectively hidden from the game,
	 * being non-existent to it. options.txt is the only expection, who still
	 * stores the disabled key binds.
	 *
	 * @param key the key binding
	 * @return {@code true} if the key binding is enabled, {@code false} otherwise
	 */
	public static boolean isEnabled(KeyBinding key) {
		return KeyBindingRegistryImpl.isEnabled(key);
	}

	/**
	 * Sets the state of the key binding.
	 *
	 * <p>This method allows for disabling the key binding, making it invisible
	 * to the game. options.txt is the only expection, who still stores the disabled
	 * key binds.
	 *
	 * @param key the key binding
	 * @param enabled the new state
	 */
	public static void setEnabled(KeyBinding key, boolean enabled) {
		KeyBindingRegistryImpl.setEnabled(key, enabled);
	}
}
