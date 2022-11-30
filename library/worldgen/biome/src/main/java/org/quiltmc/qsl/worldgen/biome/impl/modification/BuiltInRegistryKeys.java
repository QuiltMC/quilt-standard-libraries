/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.impl.modification;

import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class for getting the registry keys of built-in worldgen objects and throwing proper exceptions if they
 * are not registered.
 */
@ApiStatus.Internal
public final class BuiltInRegistryKeys {
	private static final HolderLookup.Provider vanillaRegistries = VanillaDynamicRegistries.createLookup();

	private BuiltInRegistryKeys() {
	}

	public static boolean isBuiltinBiome(RegistryKey<Biome> key) {
		return biomeRegistryWrapper().getHolder(key).isPresent();
	}

	public static HolderLookup<Biome> biomeRegistryWrapper() {
		return vanillaRegistries.getLookupOrThrow(Registries.BIOME_WORLDGEN);
	}

}
