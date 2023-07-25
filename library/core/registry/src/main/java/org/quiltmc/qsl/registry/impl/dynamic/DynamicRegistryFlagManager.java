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

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import org.quiltmc.qsl.registry.api.dynamic.DynamicRegistryFlag;

@ApiStatus.Internal
public final class DynamicRegistryFlagManager {
	private static final Multimap<RegistryKey<Registry<?>>, DynamicRegistryFlag> DYNAMIC_REGISTRY_FLAGS =
			MultimapBuilder.hashKeys().enumSetValues(DynamicRegistryFlag.class).build();

	public static void setFlag(RegistryKey<Registry<?>> key, DynamicRegistryFlag flag) {
		if (!DynamicMetaRegistryImpl.isModdedRegistryId(key.getValue())) return; // do nothing if provided key isn't for a modded registry
		DYNAMIC_REGISTRY_FLAGS.put(key, flag);
	}

	public static EnumSet<DynamicRegistryFlag> getFlags(RegistryKey<Registry<?>> key) {
		return EnumSet.copyOf(DYNAMIC_REGISTRY_FLAGS.get(key));
	}

	public static boolean isOptional(RegistryKey<Registry<?>> key) {
		return DYNAMIC_REGISTRY_FLAGS.get(key).contains(DynamicRegistryFlag.OPTIONAL);
	}

	public static boolean isOptional(Identifier registryId) {
		//creates Map of all RegistryKey getValue Identifiers to the RegistryKey said Identifier is the getValue result of
		Map<Identifier, RegistryKey<Registry<?>>> keyMap = DYNAMIC_REGISTRY_FLAGS.keySet().stream().collect(Collectors.toMap(RegistryKey::getValue, valueKey -> valueKey));
		if (keyMap.containsKey(registryId)) {
			return DYNAMIC_REGISTRY_FLAGS.get(keyMap.get(registryId)).contains(DynamicRegistryFlag.OPTIONAL);
		} else {
			return false;
		}
	}
}
