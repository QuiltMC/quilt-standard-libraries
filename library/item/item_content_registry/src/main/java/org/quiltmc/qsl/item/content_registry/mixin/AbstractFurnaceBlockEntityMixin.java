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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.SharedConstants;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
	/**
	 * @author QSL Item Content Registry
	 * @reason Not sure.
	 */
	@Overwrite
	private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible itemConvertable, int fuelTime) {
		Item item = itemConvertable.asItem();
		if (isNonFlammableWood(item)) {
			if (SharedConstants.isDevelopment) {
				throw Util.throwOrPause(
						new IllegalStateException(
								"A developer tried to explicitly make fire resistant item " + item.getName(null).getString() + " a furnace fuel. That will not work!"
						)
				);
			}
		} else {
			ItemContentRegistries.FUEL_TIME.put(item, fuelTime);
		}
	}

	/**
	 * @author QSL Item Content Registry
	 * @reason Not sure.
	 */
	@Overwrite
	private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tagKey, int fuelTime) {
		for(Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tagKey)) {
			if (!isNonFlammableWood(holder.value())) {
				if (SharedConstants.isDevelopment) {
					throw Util.throwOrPause(
							new IllegalStateException(
									"A developer tried to explicitly make fire resistant item " + holder.value().getName(null).getString() + " a furnace fuel. That will not work!"
							)
					);
				}
			}
		}

		ItemContentRegistries.FUEL_TIME.put(tagKey, fuelTime);
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
	private static boolean isNonFlammableWood(Item item) {
		throw new AssertionError();
	}
}
