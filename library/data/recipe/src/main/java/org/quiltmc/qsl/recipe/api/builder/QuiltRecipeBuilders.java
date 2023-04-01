/*
 * Copyright 2023 QuiltMC
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

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

/**
 * Provides some recipe builders for Quilt recipes.
 */
public final class QuiltRecipeBuilders {
	private QuiltRecipeBuilders() {
		throw new UnsupportedOperationException("QuiltRecipeBuilders only contains static definitions.");
	}

	/**
	 * Returns a new custom potion brewing recipe builder.
	 *
	 * @param input the input {@link Potion}
	 * @param output the output {@link Potion}
	 * @return the builder
	 */
	public static CustomPotionBrewingRecipeBuilder customPotionRecipe(Potion input, Potion output) {
		return new CustomPotionBrewingRecipeBuilder(input, output);
	}

	/**
	 * Returns a new potion brewing recipe builder.
	 *
	 * @param input the input {@link Potion}
	 * @param output the output {@link Potion}
	 * @return the builder
	 */
	public static PotionBrewingRecipeBuilder potionRecipe(Potion input, Potion output) {
		return new PotionBrewingRecipeBuilder(input, output);
	}

	/**
	 * Returns a new potion item brewing recipe builder.
	 *
	 * @param input the input {@link Item}
	 * @param output the output {@link Item}
	 * @return the builder
	 */
	public static PotionItemBrewingRecipeBuilder potionItemRecipe(Item input, Item output) {
		return new PotionItemBrewingRecipeBuilder(input, output);
	}
}
