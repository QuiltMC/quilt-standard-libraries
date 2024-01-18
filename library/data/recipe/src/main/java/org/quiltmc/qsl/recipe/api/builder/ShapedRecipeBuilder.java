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

package org.quiltmc.qsl.recipe.api.builder;

import java.util.Optional;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.unmapped.C_vhpbjodz;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**
 * Builder to build shaped crafting recipes.
 */
public class ShapedRecipeBuilder extends RecipeBuilder<ShapedRecipeBuilder, ShapedRecipe> {
	private final String[] pattern;
	private final int width;
	private final int height;
	private final Char2ObjectMap<Ingredient> ingredients = new Char2ObjectOpenHashMap<>();
	private CraftingCategory category = CraftingCategory.MISC;

	/**
	 * Creates a new shaped recipe builder.
	 *
	 * @param pattern the pattern of the shaped recipe. Each string in this array is a line of ingredients.
	 *                A character represents an ingredient and space is no ingredient
	 */
	public ShapedRecipeBuilder(String... pattern) {
		this.pattern = pattern;
		this.width = pattern[0].length();
		this.height = pattern.length;
		this.ingredients.put(' ', Ingredient.EMPTY); // By default, space is an empty ingredient.
	}

	/**
	 * Associates an ingredient with a specified key.
	 *
	 * @param key        the key of the ingredient
	 * @param ingredient the ingredient
	 * @return this builder
	 */
	public ShapedRecipeBuilder ingredient(char key, Ingredient ingredient) {
		boolean success = false;

		for (String line : this.pattern) {
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);

				if (c == key) {
					this.ingredients.put(key, ingredient);

					success = true;
					break;
				}
			}

			if (success) break;
		}

		if (!success) {
			throw new IllegalArgumentException("The pattern key '" + key + "' doesn't exist in the given pattern.");
		}

		return this;
	}

	/**
	 * Puts the specified items as the accepted ingredient at the specified key.
	 *
	 * @param key   the key of the ingredient
	 * @param items the items as ingredient
	 * @return this builder
	 * @see #ingredient(char, Ingredient)
	 * @see Ingredient#ofItems(ItemConvertible...)
	 */
	public ShapedRecipeBuilder ingredient(char key, ItemConvertible... items) {
		return this.ingredient(key, Ingredient.ofItems(items));
	}

	/**
	 * Puts the specified item tag as the accepted ingredient at the specified key.
	 *
	 * @param key the key of the ingredient
	 * @param tag the item tag as ingredient
	 * @return this builder
	 * @see #ingredient(char, Ingredient)
	 * @see Ingredient#ofTag(TagKey) (TagKey)
	 */
	public ShapedRecipeBuilder ingredient(char key, TagKey<Item> tag) {
		return this.ingredient(key, Ingredient.ofTag(tag));
	}

	/**
	 * Puts the specified item stacks as the accepted ingredient at the specified key.
	 *
	 * @param key    the key of the ingredient
	 * @param stacks the item stacks as ingredient
	 * @return this builder
	 * @see #ingredient(char, Ingredient)
	 * @see Ingredient#ofStacks(ItemStack...)
	 */
	public ShapedRecipeBuilder ingredient(char key, ItemStack... stacks) {
		return this.ingredient(key, Ingredient.ofStacks(stacks));
	}

	/**
	 * Sets the crafting book category of this recipe.
	 * <p>
	 * Default value is {@link CraftingCategory#MISC}.
	 *
	 * @param category the category
	 * @return this builder
	 */
	public ShapedRecipeBuilder category(CraftingCategory category) {
		this.category = category;
		return this;
	}

	/**
	 * Builds the shaped crafting recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the shaped recipe
	 */
	@Override
	public RecipeHolder<ShapedRecipe> build(Identifier id, String group) {
		this.checkOutputItem();
		DefaultedList<Ingredient> ingredients = VanillaRecipeBuilders.getIngredients(this.pattern, this.ingredients, this.width, this.height);
		return new RecipeHolder<>(id, new ShapedRecipe(group, this.category, new C_vhpbjodz(this.width, this.height, ingredients, Optional.empty()), this.output));
	}
}
