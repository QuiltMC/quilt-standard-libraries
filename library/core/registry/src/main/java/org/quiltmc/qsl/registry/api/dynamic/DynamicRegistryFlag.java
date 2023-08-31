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

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;
import org.quiltmc.qsl.registry.impl.dynamic.DynamicRegistryFlagManager;

/**
 * Flags that can be set on dynamic registries to define their behavior.
 * <p>
 * All flags are off by default, and can be enabled using static methods in this class or on dynamic registry creation via varargs.
 *
 * @see org.quiltmc.qsl.registry.api.sync.RegistrySynchronization  org.quiltmc.qsl.registry.api.sync.RegistrySynchronization,
 * which contains similar flag setters/getters for static registries
 */
public enum DynamicRegistryFlag {
	/**
	 * Indicates that this registry (and the entries within) do not necessarily need to be sent
	 * to (logical) clients for synchronization in multiplayer contexts.
	 * <p>
	 * <b>Note:</b> This flag is intended only for synchronized dynamic registries. On non-synced dynamic registries, this flag does nothing.
	 * <p>
	 * One use-case for this flag is for creating mods that are entirely compatible with vanilla, and thus do not
	 * require the dynamic registry to exist clientside to connect to the server.
	 * This allows for both vanilla clients/clients without the mod to connect <i>and</i> for clients with the mod
	 * supplying the registry to connect, with the latter being able to see the contents of the registry and possibly
	 * enable extra clientside features accordingly.
	 */
	OPTIONAL;

	/**
	 * Enables a specific flag on a dynamic registry.
	 *
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the target dynamic registry
	 * @param flag the flag value to enable on the dynamic registry
	 */
	public static void setFlag(Identifier registryId, DynamicRegistryFlag flag) {
		try {
			DynamicRegistryFlagManager.setFlag(registryId, flag);
		} catch (Exception e) {
			logFlagModifyException(registryId, flag, e);
		}
	}

	/**
	 * Checks if a dynamic registry has the {@link DynamicRegistryFlag#OPTIONAL} flag enabled on it.
	 *
	 * @param registryId the value id ({@link RegistryKey#getValue()}) of the dynamic registry to check
	 * @return whether the checked dynamic registry has the {@link DynamicRegistryFlag#OPTIONAL} flag enabled
	 */
	public static boolean isOptional(Identifier registryId) {
		return DynamicRegistryFlagManager.isOptional(registryId);
	}

	/**
	 * Helper method for logging exceptions to avoid code duplication.
	 */
	private static void logFlagModifyException(Identifier registryId, DynamicRegistryFlag flag, Exception e) {
		DynamicMetaRegistryImpl.LOGGER.error("Caught exception while attempting to enable flag {} on registry id {}: {}", flag.toString(), registryId, e.toString());
	}
}
