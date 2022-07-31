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
	private static boolean tryReturnItemToInventory(ItemStack remainder, DefaultedList<ItemStack> inventory, int slot) {
		ItemStack leftovers = inventory.get(slot);
		if (leftovers.isEmpty()) {
			inventory.set(slot, remainder);
			return false;
		} else if (ItemStack.canCombine(remainder, leftovers) && leftovers.getCount() + remainder.getCount() < leftovers.getMaxCount()) {
			leftovers.setCount(leftovers.getCount() + remainder.getCount());
			return false;
		}
		return true;
	}

	public static ItemStack getRemainder(ItemStack usedItem, Recipe<?> recipe) {
		ItemStack remainder = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER.get(usedItem.getItem()).getRecipeRemainder(
				usedItem,
				recipe
		);

		return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
	}

	public static void handleRemainderForNonPlayerCraft(ItemStack remainder, DefaultedList<ItemStack> inventory, int slot, World world, BlockPos location) {
		if (remainder.isEmpty()) {
			return;
		}

		if (tryReturnItemToInventory(remainder, inventory, slot)) {
			ItemScatterer.spawn(world, location.getX(), location.getY(), location.getZ(), remainder);
		}
	}

	public static void handleRemainderForPlayerCraft(ItemStack remainder, DefaultedList<ItemStack> inventory, int slot, PlayerEntity player) {
		if (remainder.isEmpty()) {
			return;
		}

		if (tryReturnItemToInventory(remainder, inventory, slot)) {
			player.getInventory().offerOrDrop(remainder);
		}
	}
}
