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

import static java.util.Objects.requireNonNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireNonEmpty;
import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireResult;
import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireSpecified;

import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.serialization.DataResult;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.CraftingRecipeInput;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.recipe.impl.RecipeDataUtil;

/**
 * Represents a shapeless crafting recipe.
 */
public final class ShapelessRecipeData implements RecipeData<CraftingRecipeInput, ShapelessRecipe> {
	private static final int MIN_INGREDIENTS = 1;
	private static final int MAX_INGREDIENTS = 9;

	static final String DEFAULT_GROUP = RecipeDataUtil.DEFAULT_GROUP;
	static final CraftingCategory DEFAULT_CATEGORY = RecipeDataUtil.DEFAULT_CRAFTING_CATEGORY;

	/**
	 * Creates a new shapeless recipe data instance.
	 *
	 * @see #builder()
	 */
	public static ShapelessRecipeData of(
			@NotNull
			String group,
			@NotNull
			CraftingCategory category,
			@NotNull
			ImmutableList<IngredientData> ingredients,
			@NotNull
			ItemStack result
	) {
		requireSpecified(ingredients, "ingredients");
		final int ingredientCount = ingredients.size();

		if (ingredientCount < MIN_INGREDIENTS || ingredientCount > MAX_INGREDIENTS) {
			throw new IllegalArgumentException(
				"there must be between %s and %s ingredients; there were: %s"
					.formatted(MIN_INGREDIENTS, MAX_INGREDIENTS, ingredientCount)
			);
		}

		return new ShapelessRecipeData(
			requireSpecified(group, "group"),
			requireSpecified(category, "category"),
			ingredients,
			requireResult(result).copy()
		);
	}

	/**
	 * Creates a {@link ShapelessRecipeData.Builder} to aid in creating shapeless recipe data instances.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final String group;
	private final CraftingCategory category;
	private final ImmutableList<IngredientData> ingredients;
	private final ItemStack result;

	private ShapelessRecipeData(
			@NotNull
			String group,
			@NotNull
			CraftingCategory category,
			@NotNull
			ImmutableList<IngredientData> ingredients,
			@NotNull
			ItemStack result
	) {
		this.group = group;
		this.category = category;
		this.ingredients = ingredients;
		this.result = result;
	}

	@Override
	public DataResult<ShapelessRecipe> createRecipe(HolderLookup.Provider registries) {
		final ImmutableList<DataResult<Ingredient>> ingredientResults = this.ingredients.stream()
				.map(data -> data.createIngredient(registries))
				.collect(toImmutableList());

		final String errorMessage = ingredientResults.stream()
				.map(DataResult::error)
				.flatMap(Optional::stream)
				.map(DataResult.Error::message)
				.collect(Collectors.joining(", "));

		if (!errorMessage.isEmpty()) {
			return DataResult.error(() -> errorMessage);
		}

		final ImmutableList<Ingredient> ingredients = ingredientResults.stream()
				.map(DataResult::getOrThrow)
				.collect(toImmutableList());

		return DataResult.success(new ShapelessRecipe(
			this.group,
			this.category,
			this.result,
			ingredients
		));
	}

	/**
	 * Convenience class for creating {@link ShapelessRecipeData} instances.
	 */
	public static final class Builder {
		private String group = DEFAULT_GROUP;
		private CraftingCategory category = DEFAULT_CATEGORY;
		private final ImmutableList.Builder<IngredientData> ingredients = ImmutableList.builder();
		private ItemStack result;

		/**
		 * Sets the recipe's group.
		 *
		 * <p>The default value is {@value DEFAULT_GROUP}.
		 *
		 * @param group the group
		 *
		 * @return this builder
		 */
		public Builder group(String group) {
			this.group = requireNonNull(group);
			return this;
		}

		/**
		 * Sets the recipe's category.
		 *
		 * <p>The default value is {@link #DEFAULT_CATEGORY}.
		 *
		 * @param category the category
		 *
		 * @return this builder
		 */
		public Builder category(CraftingCategory category) {
			this.category = requireNonNull(category);
			return this;
		}

		/**
		 * Adds an ingredient accepting the passed {@code items} to the recipe.
		 *
		 * <p>Between {@value MIN_INGREDIENTS} and {@value MAX_INGREDIENTS} must be specified before
		 * {@linkplain #build() building}.
		 *
		 * @param items the items the ingredient will accept
		 *
		 * @return this builder
		 *
		 * @see #ingredient(Item...)
		 * @see #ingredient(TagKey)
		 */
		public Builder ingredient(Iterable<Item> items) {
			this.ingredients.add(IngredientData.of(ImmutableList.copyOf(requireNonEmpty(items, "ingredient"))));
			return this;
		}

		/**
		 * @see #ingredient(Iterable)
		 * @see #ingredient(TagKey)
		 */
		public Builder ingredient(Item... items) {
			return this.ingredient(ImmutableList.copyOf(requireNonNull(items)));
		}

		/**
		 * Adds an ingredient accepting items in the passed {@code tag}.
		 *
		 * <p>Between {@value MIN_INGREDIENTS} and {@value MAX_INGREDIENTS} must be specified before
		 * {@linkplain #build() building}.
		 *
		 * @param tag the tag containing items the ingredient will accept
		 *
		 * @return this builder
		 *
		 * @see #ingredient(Iterable)
		 * @see #ingredient(Item...)
		 */
		public Builder ingredient(TagKey<Item> tag) {
			this.ingredients.add(IngredientData.of(requireNonNull(tag)));
			return this;
		}

		/**
		 * Sets the recipe's result.
		 *
		 * <p>There is no default value; a result must be specified before {@linkplain #build() building}.
		 *
		 * @param result the result
		 *
		 * @return this builder
		 */
		public Builder result(ItemStack result) {
			this.result = requireResult(result);
			return this;
		}

		/**
		 * Creates a new {@link ShapelessRecipeData} instance as specified by this builder.
		 *
		 * <p>A {@linkplain #result(ItemStack) result} and between {@value MIN_INGREDIENTS} and {@value MAX_INGREDIENTS}
		 * {@linkplain #ingredient(Iterable) ingredients} must be specified before building.
		 *
		 * @return the recipe data
		 */
		public ShapelessRecipeData build() {
			return ShapelessRecipeData.of(
				this.group,
				this.category,
				this.ingredients.build(),
				this.result
			);
		}
	}
}
