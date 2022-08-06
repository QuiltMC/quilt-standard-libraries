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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.content.registry.api.ItemContentRegistries;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends BlockEntity {
	@Shadow
	int fuel;
	@Shadow
	private DefaultedList<ItemStack> inventory;

	public BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Redirect(method = "isValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
	private boolean isValid$isFuel(ItemStack instance, Item blazePowder) {
		return isPresent(instance.getItem());
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
	private static boolean tick$isFuel(ItemStack instance, Item blazePowder) {
		return isPresent(instance.getItem());
	}

	/**
	 * Allows partial fueling up to the implied max of 20.
	 */
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick", at = @At("HEAD"))
	private static void yes(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStand, CallbackInfo ci) {
		var accessor = ((BrewingStandBlockEntityMixin) (Object) brewingStand);
		ItemStack fuel = accessor.inventory.get(4);
		int addedFuel = ItemContentRegistriesInitializer.BREWING_FUEL_MAP.getOrDefault(accessor.inventory.get(4).getItem(), 0);
		if (accessor.fuel <= 0 || (addedFuel > 0 && accessor.fuel + addedFuel <= 20)) {
			accessor.fuel += addedFuel;
			fuel.decrement(1);
			markDirty(world, pos, state);
		}
	}

	/**
	 * Only needed if {@link Items#BLAZE_POWDER blaze powder} is removed from {@link ItemContentRegistries#BREWING_FUEL_TIME}.
	 */
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", ordinal = 0))
	private static void setProperFuelTime(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStand, CallbackInfo ci) {
		var accessor = ((BrewingStandBlockEntityMixin) (Object) brewingStand);
		accessor.fuel = ItemContentRegistriesInitializer.BREWING_FUEL_MAP.getOrDefault(accessor.inventory.get(4).getItem(), 20);
	}

	/**
	 * Whether the item is in {@link ItemContentRegistries#BREWING_FUEL_TIME} and has a value greater than 0.
	 * <p>
	 * This allows a datapack to set the value of an item to 0 to remove an entry from {@link ItemContentRegistries#BREWING_FUEL_TIME}.</p>
	 */
	private static boolean isPresent(Item item) {
		return ItemContentRegistriesInitializer.BREWING_FUEL_MAP.getOrDefault(item, 0) > 0;
	}
}
