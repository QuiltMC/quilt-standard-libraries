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

package org.quiltmc.qsl.recipe.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public abstract class AbstractPotionBrewingRecipeBuilder<SELF extends AbstractPotionBrewingRecipeBuilder<SELF, TYPE, RESULT>, TYPE, RESULT extends Recipe<?>> {
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
	public AbstractPotionBrewingRecipeBuilder(TYPE input, TYPE output) {
		this.input = input;
		this.output = output;
	}

	/**
	 * Sets the required ingredient for the recipe.
	 * <p>
	 * By default, no ingredient is necessary.</p>
	 *
	 * @param ingredient the {@link Ingredient}
	 */
	@SuppressWarnings("unchecked")
	public SELF ingredient(Ingredient ingredient) {
		this.ingredient = ingredient;
		return (SELF) this;
	}

	/**
	 * Sets the required fuel for the recipe.
	 * <p>
	 * By default, the fuel is 1.</p>
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
	 * By default, the brew time is 400.</p>
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
