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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.brewing.CustomPotionBrewingRecipe;

/**
 * Builder to build custom potion recipes
 */
public class CustomPotionBrewingRecipeBuilder extends BrewingRecipeBuilder<CustomPotionBrewingRecipeBuilder, Potion, CustomPotionBrewingRecipe> {
	private final List<StatusEffectInstance> effects = new ArrayList<>();

	/**
	 * Creates a new custom potion brewing recipe builder.
	 *
	 * @param input the input {@link Potion}
	 * @param output the resulting {@link Potion}
	 */
	public CustomPotionBrewingRecipeBuilder(Potion input, Potion output) {
		super(input, output);
	}

	/**
	 * Adds the custom effects for the recipe.
	 *
	 * @param effects the custom effects to add
	 */
	public CustomPotionBrewingRecipeBuilder effects(StatusEffectInstance... effects) {
		this.effects.addAll(Arrays.asList(effects));
		return this;
	}

	/**
	 * Builds the recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the shaped recipe
	 */
	public CustomPotionBrewingRecipe build(Identifier id, String group) {
		return new CustomPotionBrewingRecipe(id, group, this.input, this.ingredient, this.output, this.fuel, this.brewTime, this.effects);
	}
}
