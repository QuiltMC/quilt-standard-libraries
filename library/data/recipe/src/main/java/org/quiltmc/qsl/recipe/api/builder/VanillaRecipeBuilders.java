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

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

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

import org.quiltmc.qsl.recipe.impl.VanillaRecipeBuildersImpl;

/**
 * Provides some recipe builders for Vanilla recipes.
 */
public final class VanillaRecipeBuilders {
	private VanillaRecipeBuilders() {
		throw new UnsupportedOperationException("VanillaRecipeBuilders only contains static definitions.");
	}

	/**
	 * Returns the list of ingredients for shaped crafting recipes.
	 *
	 * @param pattern the pattern of the shaped crafting recipe
	 * @param keys    the keys and ingredients of the recipe
	 * @param width   the width of the shaped crafting recipe
	 * @param height  the height of the shaped crafting recipe
	 * @return the ingredients
	 * @throws IllegalStateException if a key has no assigned ingredient or if there is an ingredient but no assigned key
	 */
	public static DefaultedList<Ingredient> getIngredients(String[] pattern, Char2ObjectMap<Ingredient> keys, int width, int height) {
		return VanillaRecipeBuildersImpl.getIngredients(pattern, keys, width, height);
	}

	/**
	 * Returns a new shaped crafting recipe builder.
	 *
	 * @param pattern the pattern of the shaped crafting recipe
	 * @return the builder
	 */
	public static ShapedRecipeBuilder shapedRecipe(String... pattern) {
		return new ShapedRecipeBuilder(pattern);
	}

	/**
	 * Returns a new shapeless crafting recipe builder.
	 *
	 * @param output the output stack
	 * @return the builder
	 */
	public static ShapelessRecipeBuilder shapelessRecipe(ItemStack output) {
		return new ShapelessRecipeBuilder(output);
	}

	/**
	 * Returns a new stone cutting recipe.
	 *
	 * @param id     the identifier of the recipe
	 * @param group  the group of the recipe
	 * @param input  the input ingredient
	 * @param output the output item stack
	 * @return the stone cutting recipe
	 */
	public static StonecuttingRecipe stonecuttingRecipe(Identifier id, String group, Ingredient input, ItemStack output) {
		return VanillaRecipeBuildersImpl.stonecuttingRecipe(id, group, input, output);
	}

	/**
	 * Returns a new smelting recipe.
	 *
	 * @param id         the identifier of the recipe
	 * @param group      the group of the recipe
	 * @param category   the cooking book category
	 * @param input      the input ingredient
	 * @param output     the output item stack
	 * @param experience the experience given
	 * @param cookTime   the cook time in ticks
	 * @return the smelting recipe
	 */
	public static SmeltingRecipe smeltingRecipe(Identifier id, String group, Ingredient input, CookingCategory category, ItemStack output,
			float experience, int cookTime) {
		return VanillaRecipeBuildersImpl.smeltingRecipe(id, group, category, input, output, experience, cookTime);
	}

	/**
	 * Returns a new blasting recipe.
	 *
	 * @param id         the identifier of the recipe
	 * @param group      the group of the recipe
	 * @param category   the cooking book category
	 * @param input      the input ingredient
	 * @param output     the output item stack
	 * @param experience the experience given
	 * @param cookTime   the cook time in ticks
	 * @return the blasting recipe
	 */
	public static BlastingRecipe blastingRecipe(Identifier id, String group, Ingredient input, CookingCategory category, ItemStack output,
			float experience, int cookTime) {
		return VanillaRecipeBuildersImpl.blastingRecipe(id, group, category, input, output, experience, cookTime);
	}

	/**
	 * Returns a new smoking recipe.
	 *
	 * @param id         the identifier of the recipe
	 * @param group      the group of the recipe
	 * @param category   the cooking book category
	 * @param input      the input ingredient
	 * @param output     the output item stack
	 * @param experience the experience given
	 * @param cookTime   the cook time in ticks
	 * @return the smoking recipe
	 */
	public static SmokingRecipe smokingRecipe(Identifier id, String group, CookingCategory category, Ingredient input,
			ItemStack output, float experience, int cookTime) {
		return VanillaRecipeBuildersImpl.smokingRecipe(id, group, category, input, output, experience, cookTime);
	}

	/**
	 * Returns a new campfire cooking recipe.
	 *
	 * @param id         the identifier of the recipe
	 * @param group      the group of the recipe
	 * @param category   the cooking book category
	 * @param input      the input ingredient
	 * @param output     the output item stack
	 * @param experience the experience given
	 * @param cookTime   the cook time in ticks
	 * @return the campfire cooking recipe
	 */
	public static CampfireCookingRecipe campfireCookingRecipe(Identifier id, String group, CookingCategory category, Ingredient input,
			ItemStack output, float experience, int cookTime) {
		return VanillaRecipeBuildersImpl.campfireCookingRecipe(id, group, category, input, output, experience, cookTime);
	}
}
