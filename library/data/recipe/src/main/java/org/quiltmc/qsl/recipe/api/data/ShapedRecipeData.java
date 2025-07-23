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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.DataResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.CraftingRecipeInput;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapedRecipePattern;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.recipe.impl.RecipeDataUtil;

/**
 * Represents a shaped crafting recipe.
 */
public final class ShapedRecipeData implements RecipeData<CraftingRecipeInput, ShapedRecipe> {
	private static final int MIN_WIDTH = 1;
	private static final int MAX_WIDTH = 3;
	private static final int MIN_HEIGHT = 1;
	private static final int MAX_HEIGHT = 3;

	static final String DEFAULT_GROUP = RecipeDataUtil.DEFAULT_GROUP;
	static final CraftingCategory DEFAULT_CATEGORY = RecipeDataUtil.DEFAULT_CRAFTING_CATEGORY;
	static final boolean DEFAULT_SHOW_NOTIFICATION = true;

	private final String group;
	private final CraftingCategory category;
	private final ImmutableList<String> pattern;
	private final ImmutableMap<Character, IngredientData> key;
	private final ItemStack result;
	private final boolean showNotification;

	private final int width;
	private final int height;

	/**
	 * Creates a new shaped recipe data instance.
	 *
	 * @see #builder()
	 */
	public static ShapedRecipeData of(
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
		requireSpecified(pattern, "pattern");
		requireSpecified(key, "key");

		final Dimensions patternDimensions = verifyPattern(pattern, key);

		return new ShapedRecipeData(
			requireSpecified(group, "group"),
			requireSpecified(category, "category"),
			pattern,
			patternDimensions.width, patternDimensions.height,
			key,
			requireResult(result).copy(),
			showNotification
		);
	}

	/**
	 * Creates a {@link Builder} to aid in creating shaped recipe data instances.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Verifies the pattern and its key are valid.
	 *
	 * @return the dimensions of the pattern
	 */
	private static Dimensions verifyPattern(
			ImmutableList<String> pattern,
			ImmutableMap<Character, IngredientData> key
	) {
		final int height = pattern.size();
		if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
			throw new IllegalArgumentException(
				"pattern height must be between %s and %s; was %s"
					.formatted(MIN_HEIGHT, MAX_HEIGHT, height)
			);
		}

		final Iterator<String> rowItr = pattern.iterator();
		final int width = rowItr.next().length();
		if (width < MIN_WIDTH || width > MAX_WIDTH) {
			throw new IllegalArgumentException(
				"pattern width must be between %s and %s; was %s"
					.formatted(MIN_WIDTH, MAX_WIDTH, width)
			);
		}

		while (rowItr.hasNext()) {
			final int currentWidth = rowItr.next().length();
			if (currentWidth != width) {
				throw new IllegalArgumentException(
					"all pattern rows must have the same width; found both %s and %s"
						.formatted(width, currentWidth)
				);
			}
		}

		final Set<Character> patternSymbols = new HashSet<>();
		for (int col = 0; col < height; col++) {
			for (int row = 0; row < width; row++) {
				final char symbol = pattern.get(col).charAt(row);
				if (symbol != ShapedRecipePattern.EMPTY_SLOT && !key.containsKey(symbol)) {
					throw new IllegalArgumentException("key has no mapping for " + symbol);
				} else {
					patternSymbols.add(symbol);
				}
			}
		}

		for (final Character keySymbol : key.keySet()) {
			if (!patternSymbols.contains(requireValidSymbol(keySymbol))) {
				throw new IllegalArgumentException(
					"key contains extra mapping that doesn't appear in pattern: " + keySymbol
				);
			}
		}

		return new Dimensions(width, height);
	}

	private static char requireValidSymbol(char symbol) {
		if (symbol == ShapedRecipePattern.EMPTY_SLOT) {
			throw new IllegalArgumentException(
				"key must not map '%1$s'; '%1$s' is reserved for empty slots"
					.formatted(ShapedRecipePattern.EMPTY_SLOT)
			);
		}

		return symbol;
	}

	private ShapedRecipeData(
			@NotNull
			String group,
			@NotNull
			CraftingCategory category,
			@NotNull
			ImmutableList<String> pattern,
			int width, int height,
			@NotNull
			ImmutableMap<Character, IngredientData> key,
			@NotNull
			ItemStack result,
			boolean showNotification
	) {
		this.group = group;
		this.category = category;
		this.pattern = pattern;
		this.width = width;
		this.height = height;
		this.key = key;
		this.result = result;
		this.showNotification = showNotification;
	}

	@Override
	public DataResult<ShapedRecipe> createRecipe(HolderLookup.Provider registries) {
		final ImmutableList<DataResult<Optional<Ingredient>>> ingredientResults = this.pattern.stream()
				.flatMap(row -> row.chars().mapToObj(c -> (char) c))
				.<DataResult<Optional<Ingredient>>>map(symbol -> {
					if (symbol == ShapedRecipePattern.EMPTY_SLOT) {
						return DataResult.success(Optional.empty());
					} else {
						//noinspection DataFlowIssue; verifyPattern ensures this is safe
						return this.key.get(symbol)
							.createIngredient(registries)
							.map(Optional::of);
					}
				})
				.collect(toImmutableList());

		final String errorMessage = ingredientResults.stream()
				.map(DataResult::error)
				.flatMap(Optional::stream)
				.map(DataResult.Error::message)
				.collect(Collectors.joining(", "));

		if (!errorMessage.isEmpty()) {
			return DataResult.error(() -> errorMessage);
		}

		final ImmutableList<Optional<Ingredient>> ingredients = ingredientResults.stream()
				.map(DataResult::getOrThrow)
				.collect(toImmutableList());

		return DataResult.success(new ShapedRecipe(
			this.group, this.category,
			new ShapedRecipePattern(
				this.width, this.height,
				ingredients,
				Optional.empty()
			),
			this.result,
			this.showNotification
		));
	}

	/**
	 * Convenience class for creating {@link ShapedRecipeData} instances.
	 */
	public static final class Builder {
		private String group = DEFAULT_GROUP;
		private CraftingCategory category = DEFAULT_CATEGORY;
		private ImmutableList<String> pattern;
		private final ImmutableMap.Builder<Character, IngredientData> key = ImmutableMap.builder();
		private ItemStack result;
		private boolean showNotification = DEFAULT_SHOW_NOTIFICATION;

		/**
		 * Sets the recipe's group.
		 *
		 * <p>The default value is {@value DEFAULT_GROUP}.
		 *
		 * @param group the group
		 *
		 * @return this builder
		 */
		public Builder group(@NotNull String group) {
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
		public Builder category(@NotNull CraftingCategory category) {
			this.category = requireNonNull(category);
			return this;
		}

		/**
		 * Sets the recipe's pattern.
		 *
		 * <p>There is no default value; a pattern must be specified before {@linkplain #build() building}.
		 *
		 * <p>The pattern represents how ingredients must be arranged in a crafting grid, with each string being a row
		 * and each character being a symbol representing a slot within that row. A space represents an empty slot,
		 * while other symbols represent ingredients which must be specified using one of the
		 * {@link #ingredient(char, Iterable) ingredient} methods.<br>
		 * There must be between {@value MIN_HEIGHT} and {@value MAX_HEIGHT} rows (strings), and each row mush be
		 * between {@value MIN_WIDTH} and {@value MAX_WIDTH} symbols long. All rows must have the same length.
		 *
		 * @param pattern the pattern
		 *
		 * @return this builder
		 */
		public Builder pattern(@NotNull Iterable<String> pattern) {
			this.pattern = ImmutableList.copyOf(requireNonEmpty(pattern, "pattern"));
			return this;
		}

		/**
		 * @see #pattern(Iterable)
		 */
		public Builder pattern(@NotNull String... pattern) {
			return this.pattern(ImmutableList.copyOf(requireNonNull(pattern)));
		}

		/**
		 * Associates the passed {@code symbol} with an ingredient accepting the passed {@code items}.
		 *
		 * <p>Space is a reserved symbol representing an empty slot, it cannot be associated with an ingredient.<br>
		 * Each non-space symbol in the {@linkplain #pattern(Iterable) pattern} must be mapped using this or one of the
		 * other {@code ingredient} methods.
		 *
		 * @param symbol the symbol to associate an ingredient with
		 * @param items the items the ingredient will accept
		 *
		 * @return this builder
		 *
		 * @see #ingredient(char, Item...)
		 * @see #ingredient(char, TagKey)
		 */
		public Builder ingredient(char symbol, @NotNull Iterable<Item> items) {
			this.key.put(
					requireValidSymbol(symbol),
					IngredientData.of(ImmutableList.copyOf(requireNonEmpty(items, "ingredient")))
			);
			return this;
		}

		/**
		 * @see #ingredient(char, Iterable)
		 * @see #ingredient(char, TagKey)
		 */
		public Builder ingredient(char symbol, @NotNull Item... items) {
			return this.ingredient(symbol, ImmutableList.copyOf(requireNonNull(items)));
		}

		/**
		 * Associates the passed {@code symbol} with an ingredient accepting items in the passed {@code tag}.
		 *
		 * <p>Similar to {@link #ingredient(char, Iterable)}, except that acceptable items are defined in the passed
		 * {@code tag}.
		 *
		 * @param symbol the symbol to associate an ingredient with
		 * @param tag the tag containing items the ingredient will accept
		 *
		 * @return this builder
		 *
		 * @see #ingredient(char, Iterable)
		 * @see #ingredient(char, Item...)
		 */
		public Builder ingredient(char symbol, @NotNull TagKey<Item> tag) {
			this.key.put(requireValidSymbol(symbol), IngredientData.of(requireNonNull(tag)));
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
		public Builder result(@NotNull ItemStack result) {
			this.result = requireResult(result);
			return this;
		}

		/**
		 * Sets whether the recipe should show a notification when it's unlocked.
		 *
		 * <p>The default value is {@value DEFAULT_SHOW_NOTIFICATION}.
		 *
		 * @param show whether a notification should be shown when the recipe is unlocked
		 *
		 * @return this builder
		 */
		public Builder showNotification(boolean show) {
			this.showNotification = show;
			return this;
		}

		/**
		 * Creates a new {@link ShapedRecipeData} instance as specified by this builder.
		 *
		 * <p>A {@linkplain #result(ItemStack) result} and a {@linkplain #pattern(Iterable) pattern} with all of its
		 * symbols mapped to {@linkplain #ingredient(char, Iterable) ingredients} must be specified before building.
		 *
		 * @return the recipe data
		 */
		public ShapedRecipeData build() {
			return ShapedRecipeData.of(
				this.group, this.category,
				this.pattern, this.key.build(),
				this.result,
				this.showNotification
			);
		}
	}

	private record Dimensions(int width, int height) { }
}
