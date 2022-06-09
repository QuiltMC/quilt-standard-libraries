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

package org.quiltmc.qsl.item.content_registry.mixin;

import java.util.Map;

import org.quiltmc.qsl.item.content_registry.api.ItemContentRegistries;
import org.quiltmc.qsl.item.content_registry.impl.ItemContentRegistriesInitializer;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	@Inject(method = "createFuelTimeMap", at = @At("HEAD"), cancellable = true)
	private static void returnCachedMap(CallbackInfoReturnable<Map<Item, Integer>> cir) {
		if (!ItemContentRegistriesInitializer.FUEL_MAP.isEmpty()) {
			cir.setReturnValue(ItemContentRegistriesInitializer.FUEL_MAP);
		}
	}

	@Inject(method = "createFuelTimeMap", at = @At("TAIL"))
	private static void setCachedMap(CallbackInfoReturnable<Map<Item, Integer>> cir) {
		var builtin = RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM);
		builtin.valueTable.row(ItemContentRegistries.FUEL_TIME).forEach((item, o) -> addFuel(cir.getReturnValue(), item, (Integer) o));
		builtin.valueTagTable.row(ItemContentRegistries.FUEL_TIME).forEach((tag, o) -> addFuel(cir.getReturnValue(), tag, (Integer) o));

		ItemContentRegistriesInitializer.FUEL_MAP.putAll(cir.getReturnValue());
	}

	@Inject(method = "addFuel(Ljava/util/Map;Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
	private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime, CallbackInfo ci) {
		ItemContentRegistries.FUEL_TIME.put(item.asItem(), fuelTime);
	}

	@Inject(method = "addFuel(Ljava/util/Map;Lnet/minecraft/tag/TagKey;I)V", at = @At("TAIL"))
	private static void addTagFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime, CallbackInfo ci) {
		ItemContentRegistries.FUEL_TIME.put(tag, fuelTime);
	}


	@Inject(method = "getFuelTime", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	public void quilt$getFuelTime(ItemStack fuel, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(ItemContentRegistries.FUEL_TIME.getValue(fuel.getItem()).orElse(0));
	}

	@Inject(method = "canUseAsFuel", at = @At("TAIL"), cancellable = true)
	private static void canUseAsFuel(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(ItemContentRegistries.FUEL_TIME.getValue(stack.getItem()).map(i -> i > 0).orElse(false));
	}

	@Shadow
	private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime) {
		throw new AssertionError("Not shadowed");
	}

	@Shadow
	private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime) {
		throw new AssertionError("Not shadowed");
	}
}
