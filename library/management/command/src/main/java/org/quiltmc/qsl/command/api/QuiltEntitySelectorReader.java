/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.command.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.command.EntitySelectorReader;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * An injected extension to {@link net.minecraft.command.EntitySelectorReader EntitySelectorReader}.
 * <p>
 * Allows mods to set and check arbitrary flags, useful for ensuring an entity selector option is only used once.
 *
 * @see EntitySelectorOptionRegistry
 */
@InjectedInterface(EntitySelectorReader.class)
public interface QuiltEntitySelectorReader {
	/**
	 * Gets the value for a flag.
	 *
	 * @param key   the flag name
	 * @return		the corresponding value
	 */
	boolean getFlag(@NotNull String key);

	/**
	 * Sets the value for a flag.
	 *
	 * @param key	the flag name
	 * @param value the value to set the flag to
	 */
	void setFlag(@NotNull String key, boolean value);
}
