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

import static org.quiltmc.qsl.recipe.impl.RecipeDataUtil.requireNonEmpty;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/**
 * Represents the data required to create an ingredient.
 */
public final class IngredientData {
	/**
	 * Creates a new ingredient data instance that creates an ingredient that accepts the passed {@code items}.
	 *
	 * @param items the items the ingredient will accept
	 *
	 * @return the ingredient data
	 */
	public static IngredientData of(ImmutableList<Item> items) {
		return new IngredientData(Either.left(requireNonEmpty(items, "items")));
	}

	/**
	 * Creates new ingredient data instance that creates an ingredient that accepts items in the passed {@code tag}.
	 *
	 * @param tag the tag containing the items the ingredient will accept
	 *
	 * @return the ingredient data
	 */
	public static IngredientData of(TagKey<Item> tag) {
		return new IngredientData(Either.right(tag));
	}

	private final Either<ImmutableList<Item>, TagKey<Item>> delegate;

	private IngredientData(Either<ImmutableList<Item>, TagKey<Item>> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Creates the ingredient this data represents.
	 *
	 * @param registries access to the game's registries; provides safe access to dynamic content
	 *
	 * @return the result of attempting to create the ingredient this data represents;
	 * may be an {@linkplain DataResult.Error error} if required content is missing
	 */
	public DataResult<Ingredient> createIngredient(HolderLookup.Provider registries) {
		return this.delegate.map(
			items -> DataResult.success(Ingredient.ofItems(items.toArray(Item[]::new))),
			tag -> registries.getLookupOrThrow(RegistryKeys.ITEM).getTag(tag)
				.map(Ingredient::ofItems)
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> "missing tag ingredient: " + tag.id()))
		);
	}
}
