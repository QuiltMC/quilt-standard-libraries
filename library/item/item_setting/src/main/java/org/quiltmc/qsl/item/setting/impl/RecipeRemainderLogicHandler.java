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

package org.quiltmc.qsl.item.setting.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RecipeRemainderLogicHandler {
	private static boolean tryReturnItemToInventory(ItemStack item, DefaultedList<ItemStack> inventory, int slot) {
		if (inventory.get(slot).isEmpty() || ItemStack.canCombine(item, inventory.get(slot))) {
			inventory.set(slot, item);
			return false;
		}
		return true;
	}

	public static ItemStack handleRemainderForNonPlayerCraft(ItemStack usedItem, Recipe<?> recipe, DefaultedList<ItemStack> inventory, int slot, World world, BlockPos location) {
		ItemStack remainder = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER.get(usedItem.getItem()).getRecipeRemainder(
				usedItem,
				recipe
		);

		if (remainder == ItemStack.EMPTY) {
			return ItemStack.EMPTY;
		}

		if (tryReturnItemToInventory(remainder, inventory, slot)) {
			ItemScatterer.spawn(world, location.getX(), location.getY(), location.getZ(), remainder);
		}

		return remainder;
	}

	public static ItemStack handleRemainderForPlayerCraft(ItemStack usedItem, Recipe<?> recipe, DefaultedList<ItemStack> inventory, int slot, PlayerEntity player) {
		ItemStack remainder = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER.get(usedItem.getItem()).getRecipeRemainder(
				usedItem,
				recipe
		);

		if (remainder == ItemStack.EMPTY) {
			return ItemStack.EMPTY;
		}

		if (tryReturnItemToInventory(remainder, inventory, slot)) {
			player.getInventory().offerOrDrop(remainder);
		}

		return remainder;
	}
}
