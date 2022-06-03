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

package org.quiltmc.qsl.worldgen.biome.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Holder;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.quiltmc.qsl.worldgen.biome.impl.NetherBiomeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * This Mixin is responsible for adding mod-biomes to the NETHER preset in the MultiNoiseBiomeSource.
 */
@Mixin(MultiNoiseBiomeSource.Preset.class)
public class MixinMultiNoiseBiomeSource {
	// NOTE: This is a lambda-function in the NETHER preset field initializer
	@Inject(method = "m_ixtcdgmf", at = @At("RETURN"), cancellable = true)
	private static void appendNetherBiomes(Registry<Biome> registry, CallbackInfoReturnable<MultiNoiseUtil.ParameterRangeList<Holder<Biome>>> cir) {
		MultiNoiseUtil.ParameterRangeList<Holder<Biome>> biomes = cir.getReturnValue();
		List<Pair<MultiNoiseUtil.NoiseHypercube, Holder<Biome>>> entries = new ArrayList<>(biomes.getEntries());

		// add fabric biome noise point data to list && BiomeSource biome list
		NetherBiomeData.getNetherBiomeNoisePoints().forEach((biomeKey, noisePoint) -> {
			entries.add(Pair.of(noisePoint, registry.getHolderOrThrow(biomeKey)));
		});

		cir.setReturnValue(new MultiNoiseUtil.ParameterRangeList<>(entries));
	}
}

