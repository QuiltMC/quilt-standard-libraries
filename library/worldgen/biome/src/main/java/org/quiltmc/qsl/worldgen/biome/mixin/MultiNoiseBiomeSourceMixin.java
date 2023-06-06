/*
 * Copyright 2023 QuiltMC
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

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Holder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.util.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.util.MultiNoiseBiomeSourceParameterLists;

import org.quiltmc.qsl.worldgen.biome.impl.MultiNoiseBiomeSourceParameterListHook;
import org.quiltmc.qsl.worldgen.biome.impl.NetherBiomeData;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Unique
	private static MultiNoiseUtil.ParameterRangeList<Holder<Biome>> quilt$CACHED_PARAMETER_RANGE_LIST = null;

	@Dynamic
	@Inject(
			method = "method_49505(Lnet/minecraft/registry/Holder;)Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRangeList;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void quilt$overrideNetherPreset(
			Holder<MultiNoiseBiomeSourceParameterList> holder,
			CallbackInfoReturnable<MultiNoiseUtil.ParameterRangeList<Holder<Biome>>> cir
	) {
		if (MultiNoiseBiomeSourceParameterLists.NETHER.equals(holder.getKey().orElseThrow())) {
			if (quilt$CACHED_PARAMETER_RANGE_LIST == null) {
				MultiNoiseBiomeSourceMixin.quilt$CACHED_PARAMETER_RANGE_LIST = NetherBiomeData.withModdedBiomeEntries(
						holder.value().method_49507(),
						((MultiNoiseBiomeSourceParameterListHook) holder.value()).getHolderProvider()::getHolderOrThrow
				);
			}

			cir.setReturnValue(MultiNoiseBiomeSourceMixin.quilt$CACHED_PARAMETER_RANGE_LIST);
		}
	}
}
