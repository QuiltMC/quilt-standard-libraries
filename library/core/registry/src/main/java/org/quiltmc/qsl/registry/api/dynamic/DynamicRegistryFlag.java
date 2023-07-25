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

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

/**
 * Flags that can be set on Dynamic Registries to define their behavior.
 */
public enum DynamicRegistryFlag {
	/**
	 * Indicates that this registry (and the entries within) do not necessarily need to be sent
	 * to (logical) clients for synchronization in multiplayer contexts.
	 */
	OPTIONAL;

	public static void setDynamicRegistry(RegistryKey<Registry<?>> registryKey, DynamicRegistryFlag flag) {

	}
}
