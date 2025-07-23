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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.block.entity.FuelTimes;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.registry.HolderLookup;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FuelTimes;create(Lnet/minecraft/registry/HolderLookup$Provider;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Lnet/minecraft/block/entity/FuelTimes;"))
	private FuelTimes doFuelTimeCollection(
			HolderLookup.Provider provider, FeatureFlagBitSet flags, Operation<FuelTimes> original
	) {
		ItemContentRegistriesInitializer.startInitialFuelCollection();

		FuelTimes fuelTimes = original.call(provider, flags);

		ItemContentRegistriesInitializer.endInitialFuelCollection();

		return fuelTimes;
	}
}
