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

package org.quiltmc.qsl.recipe.impl;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CookingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

@ApiStatus.Internal
public final class VanillaRecipeBuildersImpl {
	private VanillaRecipeBuildersImpl() {
		throw new UnsupportedOperationException("VanillaRecipeBuildersImpl only contains static definitions.");
	}

	public static DefaultedList<Ingredient> getIngredients(String[] pattern, Char2ObjectMap<Ingredient> keys, int width, int height) {
		DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
		var unusedKeys = new CharArraySet(keys.keySet());
		unusedKeys.remove(' ');

		for (int i = 0; i < pattern.length; ++i) {
			for (int j = 0; j < pattern[i].length(); ++j) {
				char key = pattern[i].charAt(j);
				Ingredient ingredient = keys.get(key);

				if (ingredient == null) {
					throw new IllegalStateException("Pattern references symbol '" + key + "' but it's not defined in the key");
				}

				unusedKeys.remove(key);
				ingredients.set(j + width * i, ingredient);
			}
		}

		if (!unusedKeys.isEmpty()) {
			throw new IllegalStateException("Key defines symbols that aren't used in pattern: " + unusedKeys);
		}

		return ingredients;
	}

	public static StonecuttingRecipe stonecuttingRecipe(Identifier id, String group, Ingredient input, ItemStack output) {
		if (input == Ingredient.EMPTY) throw new IllegalArgumentException("Input cannot be empty.");

		return new StonecuttingRecipe(id, group, input, output);
	}

	public static SmeltingRecipe smeltingRecipe(Identifier id, String group, CookingCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
		if (input == Ingredient.EMPTY) throw new IllegalArgumentException("Input cannot be empty.");
		if (cookTime < 0) throw new IllegalArgumentException("Cook time must be equal or greater than 0");

		return new SmeltingRecipe(id, group, category, input, output, experience, cookTime);
	}

	public static BlastingRecipe blastingRecipe(Identifier id, String group, CookingCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
		if (input == Ingredient.EMPTY) throw new IllegalArgumentException("Input cannot be empty.");
		if (cookTime < 0) throw new IllegalArgumentException("Cook time must be equal or greater than 0");

		return new BlastingRecipe(id, group, category, input, output, experience, cookTime);
	}

	public static SmokingRecipe smokingRecipe(Identifier id, String group, CookingCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
		if (input == Ingredient.EMPTY) throw new IllegalArgumentException("Input cannot be empty.");
		if (cookTime < 0) throw new IllegalArgumentException("Cook time must be equal or greater than 0");

		return new SmokingRecipe(id, group, category, input, output, experience, cookTime);
	}

	public static CampfireCookingRecipe campfireCookingRecipe(Identifier id, String group, CookingCategory category, Ingredient input,
			ItemStack output, float experience, int cookTime) {
		if (input == Ingredient.EMPTY) throw new IllegalArgumentException("Input cannot be empty.");
		if (cookTime < 0) throw new IllegalArgumentException("Cook time must be equal or greater than 0");

		return new CampfireCookingRecipe(id, group, category, input, output, experience, cookTime);
	}
}
