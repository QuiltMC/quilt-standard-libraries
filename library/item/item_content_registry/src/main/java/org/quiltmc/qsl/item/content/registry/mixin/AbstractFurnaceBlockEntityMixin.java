/*
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

package org.quiltmc.qsl.item.content.registry.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;

import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	@Shadow
	int burnTime;

	@Inject(method = "createFuelTimeMap", at = @At("HEAD"), cancellable = true)
	private static void returnCachedMap(CallbackInfoReturnable<Map<Item, Integer>> cir) {
		if (!ItemContentRegistriesInitializer.FUEL_MAP.isEmpty()) {
			cir.setReturnValue(ItemContentRegistriesInitializer.FUEL_MAP);
		}
	}

	@Inject(method = "addFuel(Ljava/util/Map;Lnet/minecraft/tag/TagKey;I)V", at = @At("HEAD"), cancellable = true)
	private static void collectInitialTags(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime, CallbackInfo ci) {
		if (ItemContentRegistriesInitializer.shouldCollectInitialTags()) {
			ItemContentRegistriesInitializer.INITIAL_FUEL_TAG_MAP.put(tag, fuelTime);
			ci.cancel();
		}
	}

	// Serializes burn time as an integer instead of a short.
	// Should not cause any desyncs as BE sync packets are now NBT.

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readBurnTimeAsInt(NbtCompound nbt, CallbackInfo info) {
		this.burnTime = nbt.getInt("BurnTime");
	}

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeBurnTimeAsInt(NbtCompound nbt, CallbackInfo info) {
		nbt.putInt("BurnTime", this.burnTime);
	}
}
