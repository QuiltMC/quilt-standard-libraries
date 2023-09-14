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

package org.quiltmc.qsl.worldgen.biome.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.HolderProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import org.quiltmc.qsl.worldgen.biome.impl.MultiNoiseSamplerExtensions;

@Mixin(RandomState.class)
public abstract class RandomStateMixin {
	@Shadow
	@Final
	private MultiNoiseUtil.MultiNoiseSampler sampler;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ChunkGeneratorSettings chunkGeneratorSettings, HolderProvider<DoublePerlinNoiseSampler.NoiseParameters> registry, long seed,
			CallbackInfo ci) {
		((MultiNoiseSamplerExtensions) (Object) this.sampler).quilt$setSeed(seed);
	}
}
