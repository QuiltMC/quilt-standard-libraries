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

package org.quiltmc.qsl.key.binds.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

// TODO - Add Javadocs; You can nab the ones from KeyBindRegistry
/**
 * An interface for adding toggling capabilities to key binds.
 */
@Environment(EnvType.CLIENT)
@InjectedInterface(KeyBind.class)
public interface ToggleableKeyBind {
	/**
	 * Gets whenever the key bind is enabled or not.
	 *
	 * @return {@code true} if the key bind is enabled, {@code false} otherwise
	 */
	default boolean isEnabled() {
		return true;
	}

	/**
	 * Gets whenever the key bind is disabled or not.
	 *
	 * @return {@code true} if the key bind is disabled, {@code false} otherwise
	 */
	default boolean isDisabled() {
		return false;
	}

	/**
	 * Gets whenever the key bind can be disabled or not.
	 *
	 * @return {@code true} if the key bind can be disabled, {@code false} otherwise
	 */
	default boolean canDisable() {
		return false;
	}

	/**
	 * Enables the key bind.
	 *
	 * <p>If the key bind has been disabled more than once, this method will only
	 * decrement its internal counter instead of enabling the key bind.
	 */
	default void enable() { }

	/**
	 * Disables the key bind.
	 *
	 * <p>When a key bind is disabled, it is effectively hidden from the game,
	 * being non-existent to it. config/quilt/key_binds.json, however, will
	 * still remember the key bind's bound keys, similar to non-existent key binds.
	 *
	 * <p>If the key bind is disabled while already disabled, it will be increment
	 * an internal counter, making the next enable only decrement it instead of
	 * enabling the key bind.
	 */
	default void disable() { }
}
