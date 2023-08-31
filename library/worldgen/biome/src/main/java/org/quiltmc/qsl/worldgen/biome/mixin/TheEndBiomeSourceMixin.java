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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import org.quiltmc.qsl.worldgen.biome.impl.TheEndBiomeData;

@Mixin(TheEndBiomeSource.class)
public abstract class TheEndBiomeSourceMixin extends BiomeSource {
	@Shadow
	@Mutable
	@Final
	public static Codec<TheEndBiomeSource> CODEC;

	@Unique
	private Supplier<TheEndBiomeData.Overrides> quilt$overrides;

	@Unique
	private boolean quilt$hasAddedBiomes = false;

	@Unique
	private boolean quilt$checkedAddedBiomes = false;

	/**
	 * Modifies the codec, so it calls the static factory method that gives us access to the
	 * full biome registry instead of just the pre-defined biomes that vanilla uses.
	 */
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void modifyCodec(CallbackInfo ci) {
		CODEC = RecordCodecBuilder.create((instance) ->
			instance.group(RegistryOps.retrieveGetter(RegistryKeys.BIOME)).apply(instance, instance.stable(TheEndBiomeSource::create)));
	}

	@Inject(method = "create", at = @At("RETURN"))
	private static void init(HolderProvider<Biome> holderProvider, CallbackInfoReturnable<TheEndBiomeSource> cir) {
		((TheEndBiomeSourceMixin) (Object) cir.getReturnValue()).quilt$overrides = Suppliers.memoize(TheEndBiomeData::createOverrides);
	}

	@Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
	private void getWeightedEndBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
		cir.setReturnValue(this.quilt$overrides.get().pick(biomeX, biomeY, biomeZ, noise, cir.getReturnValue()));
	}

	@Override
	public Set<Holder<Biome>> getBiomes() {
		var biomes = super.getBiomes();

		if (!this.quilt$checkedAddedBiomes) {
			this.quilt$checkedAddedBiomes = true;
			this.quilt$hasAddedBiomes = !this.quilt$overrides.get().getAddedBiomes().isEmpty();
		}

		if (this.quilt$hasAddedBiomes) {
			biomes = new HashSet<>(biomes);
			biomes.addAll(this.quilt$overrides.get().getAddedBiomes());
		}

		return biomes;
	}
}
