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

package org.quiltmc.qsl.registry.impl.dynamic;

import org.jetbrains.annotations.ApiStatus;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.api.dynamic.DynamicRegistryFlag;

@ApiStatus.Internal
public final class DynamicRegistryFlagManager {
	private static final Multimap<Identifier, DynamicRegistryFlag> DYNAMIC_REGISTRY_FLAGS =
			MultimapBuilder.hashKeys().enumSetValues(DynamicRegistryFlag.class).build();

	public static void setFlag(Identifier registryId, DynamicRegistryFlag flag) throws IllegalStateException {
		if (DynamicMetaRegistryImpl.isFrozen()) {
			throw new IllegalStateException("Dynamic registries are frozen, and thus flags cannot be changed!");
		}

		if (!DynamicMetaRegistryImpl.isModdedRegistryId(registryId)) return;

		DYNAMIC_REGISTRY_FLAGS.put(registryId, flag);
	}

	public static boolean isOptional(Identifier registryId) {
		if (DYNAMIC_REGISTRY_FLAGS.containsKey(registryId)) {
			return DYNAMIC_REGISTRY_FLAGS.get(registryId).contains(DynamicRegistryFlag.OPTIONAL);
		}

		return false;
	}
}
