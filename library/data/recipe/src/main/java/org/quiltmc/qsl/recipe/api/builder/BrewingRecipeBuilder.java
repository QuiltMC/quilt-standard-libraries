/*
 * Copyright 2022 QuiltMC
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
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Represents the basis of a recipe builder.
 *
 * @param <SELF>   the type of the recipe builder
 * @param <TYPE>   the type of the recipe's input and output
 * @param <RESULT> the type of the recipe
 */
public abstract class BrewingRecipeBuilder<SELF extends BrewingRecipeBuilder<SELF, TYPE, RESULT>, TYPE, RESULT extends Recipe<?>> {
	protected TYPE input;
	protected Ingredient ingredient = Ingredient.EMPTY;
	protected TYPE output;
	protected int fuel = 1;
	protected int brewTime = 400;

	/**
	 * Creates a new brewing recipe builder.
	 *
	 * @param input the input {@link TYPE}
	 * @param output the resulting {@link TYPE}
	 */
	public BrewingRecipeBuilder(TYPE input, TYPE output) {
		this.input = input;
		this.output = output;
	}

	/**
	 * Sets the required ingredient for the recipe.
	 *
	 * @param ingredient the {@link Ingredient}
	 */
	@SuppressWarnings("unchecked")
	public SELF ingredient(Ingredient ingredient) {
		this.ingredient = ingredient;
		return (SELF) this;
	}

	/**
	 * Sets items as the ingredient.
	 *
	 * @param items the items as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofItems(ItemConvertible...)
	 */
	public SELF ingredient(ItemConvertible... items) {
		return this.ingredient(Ingredient.ofItems(items));
	}

	/**
	 * Sets the specified item tag as the ingredient.
	 *
	 * @param tag the item tag as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofTag(TagKey) (TagKey)
	 */
	public SELF ingredient(TagKey<Item> tag) {
		return this.ingredient(Ingredient.ofTag(tag));
	}

	/**
	 * Sets item stacks as the ingredient.
	 *
	 * @param stacks the item stacks as ingredient
	 * @return this builder
	 * @see #ingredient(Ingredient)
	 * @see Ingredient#ofStacks(ItemStack...)
	 */
	public SELF ingredient(ItemStack... stacks) {
		return this.ingredient(Ingredient.ofStacks(stacks));
	}

	/**
	 * Sets the required fuel for the recipe.
	 * <p>
	 * By default, the fuel is {@code 1}.
	 *
	 * @param fuel the fuel to consume
	 */
	@SuppressWarnings("unchecked")
	public SELF fuel(int fuel) {
		this.fuel = fuel;
		return (SELF) this;
	}

	/**
	 * Sets the required brew time in ticks for the recipe.
	 * <p>
	 * By default, the brew time is {@code 400}.
	 *
	 * @param time the brew time
	 */
	@SuppressWarnings("unchecked")
	public SELF brewTime(int time) {
		this.brewTime = time;
		return (SELF) this;
	}

	/**
	 * Builds the recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the shaped recipe
	 */
	public abstract RESULT build(Identifier id, String group);
}
