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

import java.util.List;

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;
import org.quiltmc.qsl.key.binds.mixin.client.KeyBindAccessor;

// TODO - This isn't a registry anymore; What the heck should it be named?
/**
 * Handles the registration of modded key binds and allows for changing their properties,
 * such as their state.
 *
 * <p>This class also allows for getting key binds registered by other mods.
 */
@Environment(EnvType.CLIENT)
public class KeyBindRegistry {
	// TODO - Shouldn't we use Vanilla's Map for getAllKeyBinds instead? Two birds, one stone, zero thoughts, head empty
	/**
	 * Searches for a modded key bind with the specified translation key in the registry.
	 *
	 * @param translationKey the key bind's translation key
	 * @return the key bind if found, {@code null} otherwise
	 */
	public static KeyBind getKeyBind(String translationKey) {
		return KeyBindRegistryImpl.getKeyBind(translationKey);
	}

	// TODO - Would a transitive AW be safe for getBoundKey?
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
		return ((KeyBindAccessor) key).getBoundKey();
	}

	/**
	 * Returns a list containing all key binds.
	 *
	 * @return a list containing key binds
	 */
	public static List<KeyBind> getAllKeyBinds() {
		return KeyBindRegistryImpl.getAllKeyBinds();
	}
}
