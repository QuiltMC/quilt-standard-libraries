/*
 * Copyright 2022 The Quilt Project
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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@ApiStatus.Internal
public final class RecipeRemainderLogicHandlerImpl implements RecipeRemainderLogicHandler {
	/**
	 * {@return {@code true} if returning the item to the inventory was successful, or {@code false} if additional handling for the remainder is needed}
	 */
	@Contract(mutates = "param1, param2")
	private static boolean tryReturnItemToInventory(ItemStack remainder, DefaultedList<ItemStack> inventory, int index) {
		ItemStack leftovers = inventory.get(index);
		if (leftovers.isEmpty()) {
			inventory.set(index, remainder);
			return true;
		}

		return tryMergeStacks(leftovers, remainder);
	}

	/**
	 * {@return {@code true} if returning the item to the slot was successful, or {@code false} if additional handling for the remainder is needed}
	 */
	@Contract(mutates = "param1, param2")
	private static boolean tryReturnItemToSlot(ItemStack remainder, Slot slot) {
		ItemStack leftovers = slot.getStack();
		if (leftovers.isEmpty()) {
			slot.setStack(remainder);
			return true;
		}

		return tryMergeStacks(leftovers, remainder);
	}

	/**
	 * {@return {@code true} if the remainder stack was fully merged into the base stack, or {@code false} otherwise}
	 */
	@Contract(mutates = "param1, param2")
	private static boolean tryMergeStacks(ItemStack base, ItemStack remainder) {
		if (remainder.isEmpty()) {
			return true;
		} else if (!ItemStack.canCombine(base, remainder)) {
			return false;
		}

		int toTake = Math.min(base.getMaxCount() - base.getCount(), remainder.getCount());
		remainder.decrement(toTake);
		base.increment(toTake);
		return remainder.isEmpty();
	}

	@Contract(mutates = "param1")
	private static ItemStack decrementWithRemainder(ItemStack original, int amount, @Nullable Recipe<?> recipe) {
		if (original.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack remainder = RecipeRemainderLogicHandler.getRemainder(original, recipe);

		original.decrement(amount);

		return remainder;
	}

	@Contract(mutates = "param1, param4, param6")
	public static void handleRemainderForNonPlayerCraft(ItemStack input, int amount, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> inventory, int index, World world, BlockPos location) {
		ItemStack remainder = decrementWithRemainder(input, amount, recipe);

		if (!tryReturnItemToInventory(remainder, inventory, index)) {
			ItemScatterer.spawn(world, location.getX(), location.getY(), location.getZ(), remainder);
		}
	}

	@Contract(mutates = "param1, param4")
	public static void handleRemainderForScreenHandler(Slot slot, int amount, @Nullable Recipe<?> recipe, PlayerEntity player) {
		ItemStack remainder = decrementWithRemainder(slot.getStack(), amount, recipe);

		if (!tryReturnItemToSlot(remainder, slot)) {
			player.getInventory().offerOrDrop(remainder);
		}

		slot.markDirty();
	}
}
