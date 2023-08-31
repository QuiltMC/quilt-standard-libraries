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

package org.quiltmc.qsl.worldgen.biome.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.DensityFunctions;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.noise.NoiseRouter;

import org.quiltmc.qsl.worldgen.biome.impl.MultiNoiseSamplerExtensions;

@Mixin(ChunkNoiseSampler.class)
public abstract class ChunkNoiseSamplerMixin {
	@Unique
	private long quilt$seed;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(
			int horizontalSize,
			RandomState randomState,
			int i,
			int j,
			GenerationShapeConfig generationShapeConfig,
			DensityFunctions.StructureWeightSamplerOrMarker structureWeightSamplerOrMarker,
			ChunkGeneratorSettings chunkGeneratorSettings,
			AquiferSampler.FluidPicker fluidPicker,
			Blender blender,
			CallbackInfo ci
	) {
		this.quilt$seed = ((MultiNoiseSamplerExtensions) (Object) randomState.getSampler()).quilt$getSeed();
	}

	@Inject(method = "createMultiNoiseSampler", at = @At("RETURN"))
	private void createMultiNoiseSampler(NoiseRouter noiseRouter, List<MultiNoiseUtil.NoiseHypercube> list, CallbackInfoReturnable<MultiNoiseUtil.MultiNoiseSampler> cir) {
		((MultiNoiseSamplerExtensions) (Object) cir.getReturnValue()).quilt$setSeed(this.quilt$seed);
	}
}
