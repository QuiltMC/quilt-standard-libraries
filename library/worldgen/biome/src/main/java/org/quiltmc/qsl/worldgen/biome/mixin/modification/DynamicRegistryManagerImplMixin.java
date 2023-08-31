/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.mixin.modification;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.registry.DynamicRegistryManager;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationImpl;
import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationMarker;

/**
 * This Mixin allows us to keep backup copies of biomes for
 * {@link BiomeModificationImpl} on a per-DynamicRegistryManager basis.
 */
@Mixin(DynamicRegistryManager.ImmutableRegistryManager.class)
public abstract class DynamicRegistryManagerImplMixin implements BiomeModificationMarker {
	@Unique
	private boolean quilt$modified;

	@Override
	public void quilt$markModified() {
		if (this.quilt$modified) {
			throw new IllegalStateException("This dynamic registries instance has already been modified");
		}

		this.quilt$modified = true;
	}
}
