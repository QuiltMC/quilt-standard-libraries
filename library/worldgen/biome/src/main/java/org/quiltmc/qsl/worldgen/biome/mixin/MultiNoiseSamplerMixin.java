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

package org.quiltmc.qsl.worldgen.biome.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import org.quiltmc.qsl.worldgen.biome.impl.MultiNoiseSamplerExtensions;

@Mixin(MultiNoiseUtil.MultiNoiseSampler.class)
public class MultiNoiseSamplerMixin implements MultiNoiseSamplerExtensions {
	@Unique
	private Long quilt$seed = null;

	@Override
	public Long quilt$getSeed() {
		return this.quilt$seed;
	}

	@Override
	public void quilt$setSeed(long seed) {
		this.quilt$seed = seed;
	}
}
