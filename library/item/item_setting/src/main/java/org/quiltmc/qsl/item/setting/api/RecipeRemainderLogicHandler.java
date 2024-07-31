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

package org.quiltmc.qsl.item.setting.api;

import java.util.Map;
import java.util.function.Consumer;

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

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;
import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandlerImpl;

/**
 * Handles most logic for stack-aware recipe remainders.
 * This can be used by custom {@link Recipe} crafting systems.
 */
@ApiStatus.NonExtendable
public interface RecipeRemainderLogicHandler {
	/**
	 * Gets the stack-aware remainder of the provided {@link ItemStack} for the provided {@link Recipe}.
	 *
	 * @param original the stack to decrement
	 * @param recipe   the recipe being used
	 * @param location the remainder location
	 * @return the recipe remainder
	 */
	static ItemStack getRemainder(ItemStack original, @Nullable Recipe<?> recipe, RecipeRemainderLocation location) {
		Map<RecipeRemainderLocation, RecipeRemainderProvider> providers = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER
				.get(original.getItem());

		RecipeRemainderProvider provider = (_original, _recipe) -> _original.getItem().hasRecipeRemainder() ? _original.getItem().getRecipeRemainder().getDefaultStack() : ItemStack.EMPTY;

		if (RecipeRemainderLogicHandlerImpl.DEFAULT_LOCATIONS.contains(location) && providers.containsKey(RecipeRemainderLocation.DEFAULT_LOCATIONS)) {
			provider = providers.get(RecipeRemainderLocation.DEFAULT_LOCATIONS);
		}

		if (providers.containsKey(location)) {
			provider = providers.get(location);
		}

		if (providers.containsKey(RecipeRemainderLocation.ALL_LOCATIONS)) {
			provider = providers.get(RecipeRemainderLocation.ALL_LOCATIONS);
		}

		ItemStack remainder = provider.getRecipeRemainder(
					original,
					recipe
				);

		return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
	}

	/**
	 * Handles the recipe remainder logic for crafts without a {@link PlayerEntity player} present.
	 * Excess items that cannot be returned to a slot are dropped in the world.
	 *
	 * @param input     the original item stack
	 * @param amount    the amount by which to decrease the stack
	 * @param recipe    the recipe being used
	 * @param location  the remainder location
	 * @param inventory the inventory
	 * @param index     the index of the original stack in the inventory
	 * @param world     the world
	 * @param pos       the location to drop excess remainders
	 */
	@Contract(mutates = "param1, param5, param7")
	static void handleRemainderForNonPlayerCraft(ItemStack input, int amount, @Nullable Recipe<?> recipe, RecipeRemainderLocation location, DefaultedList<ItemStack> inventory, int index, World world, BlockPos pos) {
		handleRemainderForNonPlayerCraft(input, amount, recipe, location, inventory, index, remainder -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainder));
	}

	/**
	 * Handles the recipe remainder logic for crafts without a {@link PlayerEntity player} present.
	 * Excess items that cannot be returned to a slot are handled by the provided {@link Consumer consumer}.
	 *
	 * @param input     the original item stack
	 * @param amount    the amount by which to decrease the stack
	 * @param recipe    the recipe being used
	 * @param location  the remainder location
	 * @param inventory the inventory
	 * @param index     the index of the original stack in the inventory
	 * @param failure   callback that is run if excess items could not be returned to a slot
	 */
	@Contract(mutates = "param1, param5, param7")
	static void handleRemainderForNonPlayerCraft(ItemStack input, int amount, @Nullable Recipe<?> recipe, RecipeRemainderLocation location, DefaultedList<ItemStack> inventory, int index, Consumer<ItemStack> failure) {
		RecipeRemainderLogicHandlerImpl.handleRemainderForNonPlayerCraft(input, amount, recipe, location, inventory, index, failure);
	}

	/**
	 * @see RecipeRemainderLogicHandler#handleRemainderForNonPlayerCraft(ItemStack, int, Recipe, RecipeRemainderLocation, DefaultedList, int, World, BlockPos)
	 */
	@Contract(mutates = "param1, param4, param6")
	static void handleRemainderForNonPlayerCraft(ItemStack input, @Nullable Recipe<?> recipe, RecipeRemainderLocation location, DefaultedList<ItemStack> inventory, int index, World world, BlockPos pos) {
		handleRemainderForNonPlayerCraft(input, 1, recipe, location, inventory, index, world, pos);
	}

	/**
	 * Handles the recipe remainder logic for crafts within a {@link net.minecraft.screen.ScreenHandler screen handler}.
	 * Excess items that cannot be returned to a slot are {@linkplain net.minecraft.entity.player.PlayerInventory#offerOrDrop(ItemStack) offered to the player or dropped}.
	 *
	 * @param slot     the slot of the original stack
	 * @param amount   the amount by which to decrease the stack
	 * @param recipe   the recipe being used
	 * @param location the remainder location
	 * @param player   the player performing the craft
	 */
	@Contract(mutates = "param1, param5")
	static void handleRemainderForScreenHandler(Slot slot, int amount, @Nullable Recipe<?> recipe, RecipeRemainderLocation location, PlayerEntity player) {
		RecipeRemainderLogicHandlerImpl.handleRemainderForScreenHandler(slot, amount, recipe, location, player);
	}

	/**
	 * @see RecipeRemainderLogicHandler#handleRemainderForScreenHandler(Slot, int, Recipe, RecipeRemainderLocation, PlayerEntity)
	 */
	@Contract(mutates = "param1, param4")
	static void handleRemainderForScreenHandler(Slot slot, @Nullable Recipe<?> recipe, RecipeRemainderLocation location, PlayerEntity player) {
		handleRemainderForScreenHandler(slot, 1, recipe, location, player);
	}
}
