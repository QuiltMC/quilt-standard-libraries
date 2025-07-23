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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;

@Mixin(FuelTimes.Builder.class)
abstract class FuelTimesBuilderMixin {
	@Inject(
			method = "add(Lnet/minecraft/item/ItemConvertible;I)Lnet/minecraft/block/entity/FuelTimes$Builder;",
			at = @At("HEAD")
	)
	private void collectInitialItems(
			ItemConvertible item, int fuelTime, CallbackInfoReturnable<FuelTimes.Builder> cir
	) {
		if (ItemContentRegistriesInitializer.shouldCollectInitialFuels()) {
			ItemContentRegistriesInitializer.INITIAL_FUEL_ITEM_MAP.put(item.asItem(), fuelTime);
		}
	}

	@Inject(
			method = "add(Lnet/minecraft/registry/tag/TagKey;I)Lnet/minecraft/block/entity/FuelTimes$Builder;",
			at = @At("HEAD")
	)
	private void collectInitialTags(TagKey<Item> tag, int fuelTime, CallbackInfoReturnable<FuelTimes.Builder> cir) {
		if (ItemContentRegistriesInitializer.shouldCollectInitialFuels()) {
			ItemContentRegistriesInitializer.INITIAL_FUEL_TAG_MAP.put(tag, fuelTime);
		}
	}
}
