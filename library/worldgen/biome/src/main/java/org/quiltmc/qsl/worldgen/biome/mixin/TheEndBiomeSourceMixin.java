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

import com.google.common.base.Suppliers;
import net.minecraft.util.registry.Holder;
import net.minecraft.util.registry.HolderProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.quiltmc.qsl.worldgen.biome.impl.TheEndBiomeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(TheEndBiomeSource.class)
public abstract class TheEndBiomeSourceMixin extends BiomeSource {
	@Unique
	private Supplier<TheEndBiomeData.Overrides> overrides;

	@Unique
	private boolean quilt$hasAddedBiomes = false;

	protected TheEndBiomeSourceMixin(Stream<Holder<Biome>> stream) {
		super(stream);
	}

	@Inject(method = "m_biyltupg(Lnet/minecraft/util/registry/HolderProvider;)Lnet/minecraft/world/biome/source/TheEndBiomeSource;", at = @At("RETURN"))
	private static void init(HolderProvider<Biome> holderProvider, CallbackInfoReturnable<TheEndBiomeSource> cir) {
		((TheEndBiomeSourceMixin) (Object) cir.getReturnValue()).overrides = Suppliers.memoize(() -> TheEndBiomeData.createOverrides(holderProvider));
	}

	@Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
	private void getWeightedEndBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
		cir.setReturnValue(this.overrides.get().pick(biomeX, biomeY, biomeZ, noise, cir.getReturnValue()));
	}

	@Override
	public Set<Holder<Biome>> getBiomes() {
		if (!this.quilt$hasAddedBiomes) {
			this.quilt$hasAddedBiomes = true;
			super.getBiomes().addAll(this.overrides.get().getAddedBiomes());
		}

		return super.getBiomes();
	}
}
