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

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;

public class KeyBindingRegistry {
	public static KeyBinding registerKeyBinding(KeyBinding key) {
		return registerKeyBinding(key, true);
	}

	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		return KeyBindingRegistryImpl.registerKeyBinding(key, enabled);
	}

	// TODO - Add get method for easier intercompat

	public static boolean getEnabled(KeyBinding key) {
		return KeyBindingRegistryImpl.getEnabled(key);
	}

	public static void setEnabled(KeyBinding key, boolean newEnabled) {
		KeyBindingRegistryImpl.setEnabled(key, newEnabled);
	}
}
