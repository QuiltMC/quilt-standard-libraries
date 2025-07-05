/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.item.content.registry.mixin;

import net.minecraft.server.MinecraftServer;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FuelTimes;create(Lnet/minecraft/registry/HolderLookup$Provider;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Lnet/minecraft/block/entity/FuelTimes;"))
	private void startFuelTimeCollection(CallbackInfo ci) {
		ItemContentRegistriesInitializer.startInitialFuelCollection();
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FuelTimes;create(Lnet/minecraft/registry/HolderLookup$Provider;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Lnet/minecraft/block/entity/FuelTimes;", shift = At.Shift.AFTER))
	private void endFuelTimeCollection(CallbackInfo ci) {
		ItemContentRegistriesInitializer.endInitialFuelCollection();
	}
}
