/*
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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.HolderProvider;
import net.minecraft.unmapped.C_xmtsvelx;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.worldgen.biome.impl.C_xmtsvelxHook;

@Mixin(C_xmtsvelx.class)
public abstract class C_xmtsvelxMixin implements C_xmtsvelxHook {
	@Unique
	private HolderProvider<Biome> quilt$holderProvider;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void quilt$grabHolderProvider(C_xmtsvelx.C_jgklgqjr c_jgklgqjr, HolderProvider<Biome> holderProvider, CallbackInfo ci) {
		this.quilt$holderProvider = holderProvider;
	}

	@Override
	public HolderProvider<Biome> getHolderProvider() {
		return this.quilt$holderProvider;
	}
}
