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

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.mixin.client.GameOptionsAccessor;

public class KeyBindingManager {
	private final GameOptions options;
	private final KeyBinding[] allKeys;

	public KeyBindingManager(GameOptions options, KeyBinding[] allKeys) {
		this.options = options;
		this.allKeys = allKeys;
		this.addModdedKeyBinds();
	}

	public void addModdedKeyBinds() {
		((GameOptionsAccessor) (Object) this.options).setKeysAll(KeyBindingRegistryImpl.getKeyBindings(this.allKeys));
	}
}
