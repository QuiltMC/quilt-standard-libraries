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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**
 * Builder to build shapeless crafting recipes.
 */
public class ShapelessRecipeBuilder extends RecipeBuilder<ShapelessRecipeBuilder, ShapelessRecipe> {
	private final Set<Ingredient> ingredients = new HashSet<>();
	private CraftingCategory category = CraftingCategory.MISC;

	public ShapelessRecipeBuilder(ItemStack output) {
		this.output = output;
	}

	/**
	 * Adds an ingredient.
	 *
	 * @param ingredient the ingredient
	 * @return this builder
	 */
	public ShapelessRecipeBuilder ingredient(Ingredient ingredient) {
		this.ingredients.add(ingredient);
		return this;
	}

	/**
	 * Adds items as a single ingredient.
	 *
	 * @param items the items as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofItems(ItemConvertible...)
	 */
	public ShapelessRecipeBuilder ingredient(ItemConvertible... items) {
		return this.ingredient(Ingredient.ofItems(items));
	}

	/**
	 * Adds the specified item tag as a single ingredient.
	 *
	 * @param tag the item tag as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofTag(TagKey) (TagKey)
	 */
	public ShapelessRecipeBuilder ingredient(TagKey<Item> tag) {
		return this.ingredient(Ingredient.ofTag(tag));
	}

	/**
	 * Adds item stacks as a single ingredient.
	 *
	 * @param stacks the item stacks as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofStacks(ItemStack...)
	 */
	public ShapelessRecipeBuilder ingredient(ItemStack... stacks) {
		return this.ingredient(Ingredient.ofStacks(stacks));
	}

	/**
	 * Sets the crafting book category of this recipe.
	 * <p>
	 * Default value is {@link CraftingCategory#MISC}.
	 *
	 * @param category the category
	 * @return this builder
	 */
	public ShapelessRecipeBuilder category(CraftingCategory category) {
		this.category = category;
		return this;
	}

	/**
	 * Builds the shapeless crafting recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the shapeless crafting recipe
	 */
	public ShapelessRecipe build(Identifier id, String group) {
		this.checkOutputItem();

		if (this.ingredients.size() == 0) throw new IllegalStateException("Cannot build a recipe without ingredients.");

		DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(this.ingredients.size(), Ingredient.EMPTY);
		int i = 0;

		for (var ingredient : this.ingredients) {
			ingredients.set(i, ingredient);
			i++;
		}

		return new ShapelessRecipe(id, group, this.category, this.output, ingredients);
	}
}
