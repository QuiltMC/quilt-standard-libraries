/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.api.dynamic;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicRegistryFlagManager;

/**
 * Flags that can be set on dynamic registries to define their behavior.
 * <p>All flags default to being disabled/turned off, and can be enabled or disabled using static methods in this class.
 *
 * @see org.quiltmc.qsl.registry.api.sync.RegistrySynchronization  org.quiltmc.qsl.registry.api.sync.RegistrySynchronization,
 * which contains similar flag setters/getters for static registries
 */
public enum DynamicRegistryFlag {
	/**
	 * Note: This flag is intended only for synchronized dynamic registries. On non-synced dynamic registries, this flag does nothing.
	 * <p></p>
	 * Indicates that this registry (and the entries within) do not necessarily need to be sent
	 * to (logical) clients for synchronization in multiplayer contexts.
	 * <p>
	 * This is useful, for instance, when creating a server-sided mod that is vanilla client compatible when installed on a server,
	 * but has extra functionality for players when the mod is installed on a connecting client.
	 */
	OPTIONAL;

	/**
	 * Enables a specific flag on a dynamic registry.
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the target dynamic registry
	 * @param flag the flag value to enable on the dynamic registry
	 */
	public static void enableFlag(Identifier registryId, DynamicRegistryFlag flag) {
		DynamicRegistryFlagManager.enableFlag(registryId, flag);
	}

	/**
	 * Disables a specific flag on a dynamic registry.
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the target dynamic registry
	 * @param flag the flag value to disable on the dynamic registry
	 */
	public static void disableFlag(Identifier registryId, DynamicRegistryFlag flag) {
		DynamicRegistryFlagManager.disableFlag(registryId, flag);
	}

	/**
	 * Sets whether a dynamic registry has the {@link DynamicRegistryFlag#OPTIONAL} flag enabled or disabled.
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the target dynamic registry
	 * @param isOptional whether the targeted dynamic registry should have the {@link DynamicRegistryFlag#OPTIONAL} flag enabled or disabled
	 */
	public static void setOptional(Identifier registryId, boolean isOptional) {
		if (isOptional) {
			DynamicRegistryFlag.enableFlag(registryId, OPTIONAL);
		} else {
			DynamicRegistryFlag.disableFlag(registryId, OPTIONAL);
		}
	}

	/**
	 * Checks if a dynamic registry has the {@link DynamicRegistryFlag#OPTIONAL} flag enabled on it.
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the dynamic registry to check
	 * @return whether the checked dynamic registry has the {@link DynamicRegistryFlag#OPTIONAL} flag enabled
	 */
	public static boolean isOptional(Identifier registryId) {
		return DynamicRegistryFlagManager.isOptional(registryId);
	}
}
