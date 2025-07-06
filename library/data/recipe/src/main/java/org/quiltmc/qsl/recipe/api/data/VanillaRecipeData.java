/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.recipe.api.data;

import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireResult;
import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireSpecified;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CookingCategory;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SingleRecipeInput;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;

import org.quiltmc.qsl.recipe.impl.RecipeDataUtil;

/**
 * Provides methods for creating {@link RecipeData} representing vanilla recipe types.
 */
public final class VanillaRecipeData {
	private VanillaRecipeData() {
		throw new UnsupportedOperationException(
			VanillaRecipeData.class.getSimpleName() + "contains only static members"
		);
	}

	private static final int DEFAULT_COOKING_EXPERIENCE = 0;
	private static final int DEFAULT_SLOW_COOK_TIME = 200;
	private static final CookingCategory DEFAULT_COOKING_CATEGORY = CookingCategory.MISC;
	private static final int DEFAULT_FAST_COOK_TIME = 100;

	/**
	 * Creates a {@link ShapedRecipeData.Builder} to aid in creating {@link ShapedRecipeData} instances.
	 *
	 * @return the builder
	 *
	 * @see #createShaped(String, CraftingCategory, ImmutableList, ImmutableMap, ItemStack, boolean)
	 * @see #createShaped(ImmutableList, ImmutableMap, ItemStack)
	 */
	public static ShapedRecipeData.Builder shapedBuilder() {
		return ShapedRecipeData.builder();
	}

	/**
	 * Creates a {@link ShapelessRecipeData.Builder} to aid in creating {@link ShapelessRecipeData} instances.
	 *
	 * @return the builder
	 *
	 * @see #createShapeless(String, CraftingCategory, ImmutableList, ItemStack)
	 * @see #createShapeless(ImmutableList, ItemStack)
	 */
	public static ShapelessRecipeData.Builder shapelessBuilder() {
		return ShapelessRecipeData.builder();
	}

	/**
	 * Creates a new {@link ShapedRecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #shapedBuilder()
	 * @see #createShaped(ImmutableList, ImmutableMap, ItemStack)
	 */
	public static ShapedRecipeData createShaped(
			@NotNull
			String group,
			@NotNull
			CraftingCategory category,
			@NotNull
			ImmutableList<String> pattern,
			@NotNull
			ImmutableMap<Character, IngredientData> key,
			@NotNull
			ItemStack result,
			boolean showNotification
	) {
		return ShapedRecipeData.of(group, category, pattern, key, result, showNotification);
	}

	/**
	 * Creates a new {@link ShapedRecipeData} instance with the default values for
	 * {@linkplain ShapedRecipeData#DEFAULT_GROUP group},
	 * {@linkplain ShapedRecipeData#DEFAULT_CATEGORY category}, and
	 * {@linkplain ShapedRecipeData#DEFAULT_SHOW_NOTIFICATION showNotification}.
	 *
	 * @return the recipe data
	 *
	 * @see #shapedBuilder()
	 * @see #createShaped(String, CraftingCategory, ImmutableList, ImmutableMap, ItemStack, boolean)
	 */
	public static ShapedRecipeData createShaped(
			@NotNull
			ImmutableList<String> pattern,
			@NotNull
			ImmutableMap<Character, IngredientData> key,
			@NotNull
			ItemStack result
	) {
		return createShaped(
			ShapedRecipeData.DEFAULT_GROUP,
			ShapedRecipeData.DEFAULT_CATEGORY,
			pattern,
			key,
			result,
			ShapedRecipeData.DEFAULT_SHOW_NOTIFICATION
		);
	}

	/**
	 * Creates a new {@link ShapelessRecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #shapelessBuilder()
	 * @see #createShapeless(ImmutableList, ItemStack)
	 */
	public static ShapelessRecipeData createShapeless(
			@NotNull
			String group,
			@NotNull
			CraftingCategory category,
			@NotNull
			ImmutableList<IngredientData> ingredients,
			@NotNull
			ItemStack result
	) {
		return ShapelessRecipeData.of(group, category, ingredients, result);
	}

	/**
	 * Creates a new {@link ShapelessRecipeData} instance with the default values for
	 * {@linkplain ShapelessRecipeData#DEFAULT_GROUP group} and
	 * {@linkplain ShapelessRecipeData#DEFAULT_CATEGORY category}.
	 *
	 * @return the recipe data
	 *
	 * @see #shapelessBuilder()
	 * @see #createShapeless(String, CraftingCategory, ImmutableList, ItemStack)
	 */
	public static ShapelessRecipeData createShapeless(
			@NotNull
			ImmutableList<IngredientData> ingredients,
			@NotNull
			ItemStack result
	) {
		return createShapeless(
			ShapelessRecipeData.DEFAULT_GROUP,
			ShapelessRecipeData.DEFAULT_CATEGORY,
			ingredients,
			result
		);
	}

	/**
	 * Creates a new {@link StonecuttingRecipe} {@link RecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #createStonecutting(IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, StonecuttingRecipe> createStonecutting(
			@NotNull
			String group,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		requireSpecified(group, "group");
		requireSpecified(ingredient, "ingredient");
		final ItemStack resultCopy = requireResult(result).copy();

		return registries -> ingredient.createIngredient(registries)
			.map(resolvedIngredient -> new StonecuttingRecipe(group, resolvedIngredient, resultCopy));
	}

	/**
	 * Creates a new {@link StonecuttingRecipe} {@link RecipeData} instance with the default
	 * {@linkplain RecipeDataUtil#DEFAULT_GROUP group}.
	 *
	 * @return the recipe data
	 *
	 * @see #createStonecutting(String, IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, StonecuttingRecipe> createStonecutting(
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		return createStonecutting(RecipeDataUtil.DEFAULT_GROUP, ingredient, result);
	}

	/**
	 * Creates a new {@link SmeltingRecipe} {@link RecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #createSmelting(IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, SmeltingRecipe> createSmelting(
			@NotNull
			String group,
			@NotNull
			CookingCategory category,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result,
			float experience,
			int cookTime
	) {
		return createCookingImpl(group, category, ingredient, result, experience, cookTime, SmeltingRecipe::new);
	}

	/**
	 * Creates a new {@link SmeltingRecipe} {@link RecipeData} instance with the default values for
	 * {@linkplain RecipeDataUtil#DEFAULT_GROUP group} and {@linkplain #DEFAULT_COOKING_CATEGORY category}.
	 *
	 * @return the recipe data
	 *
	 * @see #createSmelting(String, CookingCategory, IngredientData, ItemStack, float, int)
	 */
	public static RecipeData<SingleRecipeInput, SmeltingRecipe> createSmelting(
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		return createSmelting(
			RecipeDataUtil.DEFAULT_GROUP,
			DEFAULT_COOKING_CATEGORY,
			ingredient,
			result,
			DEFAULT_COOKING_EXPERIENCE,
			DEFAULT_SLOW_COOK_TIME
		);
	}

	/**
	 * Creates a new {@link BlastingRecipe} {@link RecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #createBlasting(IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, BlastingRecipe> createBlasting(
			@NotNull
			String group,
			@NotNull
			CookingCategory category,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result,
			float experience,
			int cookTime
	) {
		return createCookingImpl(group, category, ingredient, result, experience, cookTime, BlastingRecipe::new);
	}

	/**
	 * Creates a new {@link BlastingRecipe} {@link RecipeData} instance with the default values for
	 * {@linkplain RecipeDataUtil#DEFAULT_GROUP group} and {@linkplain #DEFAULT_COOKING_CATEGORY category}.
	 *
	 * @return the recipe data
	 *
	 * @see #createBlasting(String, CookingCategory, IngredientData, ItemStack, float, int)
	 */
	public static RecipeData<SingleRecipeInput, BlastingRecipe> createBlasting(
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		return createBlasting(
			RecipeDataUtil.DEFAULT_GROUP,
			DEFAULT_COOKING_CATEGORY,
			ingredient,
			result,
			DEFAULT_COOKING_EXPERIENCE,
			DEFAULT_FAST_COOK_TIME
		);
	}

	/**
	 * Creates a new {@link SmokingRecipe} {@link RecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #createSmoking(IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, SmokingRecipe> createSmoking(
			@NotNull
			String group,
			@NotNull
			CookingCategory category,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result,
			float experience,
			int cookTime
	) {
		return createCookingImpl(group, category, ingredient, result, experience, cookTime, SmokingRecipe::new);
	}

	/**
	 * Creates a new {@link SmokingRecipe} {@link RecipeData} instance with the default values for
	 * {@linkplain RecipeDataUtil#DEFAULT_GROUP group} and {@linkplain #DEFAULT_COOKING_CATEGORY category}.
	 *
	 * @return the recipe data
	 *
	 * @see #createSmoking(String, CookingCategory, IngredientData, ItemStack, float, int)
	 */
	public static RecipeData<SingleRecipeInput, SmokingRecipe> createSmoking(
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		return createSmoking(
			RecipeDataUtil.DEFAULT_GROUP,
			DEFAULT_COOKING_CATEGORY,
			ingredient,
			result,
			DEFAULT_COOKING_EXPERIENCE,
			DEFAULT_FAST_COOK_TIME
		);
	}

	/**
	 * Creates a new {@link CampfireCookingRecipe} {@link RecipeData} instance.
	 *
	 * @return the recipe data
	 *
	 * @see #createCampfire(IngredientData, ItemStack)
	 */
	public static RecipeData<SingleRecipeInput, CampfireCookingRecipe> createCampfire(
			@NotNull
			String group,
			@NotNull
			CookingCategory category,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result,
			float experience,
			int cookTime
	) {
		return createCookingImpl(group, category, ingredient, result, experience, cookTime, CampfireCookingRecipe::new);
	}

	/**
	 * Creates a new {@link CampfireCookingRecipe} {@link RecipeData} instance with the default values for
	 * {@linkplain RecipeDataUtil#DEFAULT_GROUP group} and {@linkplain #DEFAULT_COOKING_CATEGORY category}.
	 *
	 * @return the recipe data
	 *
	 * @see #createCampfire(String, CookingCategory, IngredientData, ItemStack, float, int)
	 */
	public static RecipeData<SingleRecipeInput, CampfireCookingRecipe> createCampfire(
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result
	) {
		return createCampfire(
			RecipeDataUtil.DEFAULT_GROUP,
			DEFAULT_COOKING_CATEGORY,
			ingredient,
			result,
			DEFAULT_COOKING_EXPERIENCE,
			DEFAULT_FAST_COOK_TIME
		);
	}

	private static <R extends AbstractCookingRecipe> RecipeData<SingleRecipeInput, R> createCookingImpl(
			@NotNull
			String group,
			@NotNull
			CookingCategory category,
			@NotNull
			IngredientData ingredient,
			@NotNull
			ItemStack result,
			float experience,
			int cookTime,
			@NotNull
			CookingFactory<R> factory
	) {
		requireSpecified(group, "group");
		requireSpecified(category, "category");
		requireSpecified(ingredient, "ingredient");
		final ItemStack resultCopy = requireResult(result);

		return registries -> ingredient.createIngredient(registries).map(resolvedIngredient ->
			factory.create(group, category, resolvedIngredient, resultCopy, experience, cookTime)
		);
	}

	@FunctionalInterface
	private interface CookingFactory<R extends AbstractCookingRecipe> {
		R create(
				String group,
				CookingCategory category,
				Ingredient ingredient,
				ItemStack result,
				float experience,
				int cookTime
		);
	}
}
