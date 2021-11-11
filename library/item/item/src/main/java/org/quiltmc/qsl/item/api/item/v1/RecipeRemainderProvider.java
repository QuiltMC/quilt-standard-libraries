/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.item.api.item.v1;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.impl.CustomItemSettingImpl;

/**
 * Allows an item to conditionally specify the recipe remainder.
 * The recipe remainder is an {@link ItemStack} instead of an {@link Item}.
 * This can be used to allow your item to get damaged instead of
 * getting removed when used in crafting.
 *
 * <p>Recipe remainder providers can be set with {@link QuiltItemSettings#recipeRemainder(RecipeRemainderProvider)}.</p>
 */
@FunctionalInterface
public interface RecipeRemainderProvider {
	/**
	 * An {@link ItemStack} aware version of {@link Item#getRecipeRemainder()}.
	 *
	 * @param original the input item stack
	 * @param type the recipe type being used
	 * @return the recipe remainder
	 */
	ItemStack getRecipeRemainder(ItemStack original, @Nullable RecipeType<?> type);

	static DefaultedList<ItemStack> getRemainingStacks(Inventory inventory, @Nullable RecipeType<?> type) {
		DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack stack = inventory.getStack(i);
			ItemStack remainder = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER.get(stack.getItem()).getRecipeRemainder(stack, type);

			if (!remainder.isEmpty()) {
				defaultedList.set(i, remainder);
			}
		}

		return defaultedList;
	}
}
