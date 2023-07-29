/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import org.quiltmc.qsl.worldgen.biome.impl.NetherBiomeData;

@Mixin(targets = "net/minecraft/world/biome/util/MultiNoiseBiomeSourceParameterList$Preset$C_cnkaoojo")
public abstract class C_cnkaoojoMixin {
	@Inject(method = "apply", at = @At("RETURN"), cancellable = true, remap = false)
	public <T> void modifyNetherPreset(Function<RegistryKey<Biome>, T> function, CallbackInfoReturnable<MultiNoiseUtil.ParameterRangeList<T>> cir) {
		cir.setReturnValue(NetherBiomeData.withModdedBiomeEntries(cir.getReturnValue(), function));
	}
}
