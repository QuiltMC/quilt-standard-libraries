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

package org.quiltmc.qsl.recipe.impl;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class ModifyRecipeHandlerImpl extends BasicRecipeHandlerImpl implements RecipeLoadingEvents.ModifyRecipesCallback.RecipeHandler {
	int counter = 0;

	ModifyRecipeHandlerImpl(RecipeManager recipeManager, Map<RecipeType<?>, Map<Identifier, RecipeUnlocker<?>>> recipes,
			Map<Identifier, RecipeUnlocker<?>> globalRecipes, DynamicRegistryManager registryManager) {
		super(recipeManager, recipes, globalRecipes, registryManager);
	}

	private void add(RecipeUnlocker<?> recipeUnlocker) {
		Map<Identifier, RecipeUnlocker<?>> type = this.recipes.get(recipeUnlocker.comp_1933().getType());

		if (type == null) {
			throw new IllegalStateException("The given recipe " + recipeUnlocker.comp_1932()
					+ " does not have its recipe type " + recipeUnlocker.comp_1932() + " in the recipe manager.");
		}

		type.put(recipeUnlocker.comp_1932(), recipeUnlocker);
		this.globalRecipes.put(recipeUnlocker.comp_1932(), recipeUnlocker);
	}

	@Override
	public void replace(RecipeUnlocker<?> recipeUnlocker) {
		RecipeType<?> oldType = this.getTypeOf(recipeUnlocker.comp_1932());

		if (oldType == null) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Add new recipe {} with type {} in modify phase.", recipeUnlocker.comp_1932(), recipeUnlocker.comp_1933().getType());
			}

			this.add(recipeUnlocker);
		} else if (oldType == recipeUnlocker.comp_1933().getType()) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace recipe {} with same type {} in modify phase.", recipeUnlocker.comp_1932(), recipeUnlocker.comp_1933().getType());
			}

			this.recipes.get(oldType).put(recipeUnlocker.comp_1932(), recipeUnlocker);
			this.globalRecipes.put(recipeUnlocker.comp_1932(), recipeUnlocker);
		} else {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace new recipe {} with type {} (and old type {}) in modify phase.", recipeUnlocker.comp_1932(), recipeUnlocker.comp_1933().getType(), oldType);
			}

			this.recipes.get(oldType).remove(recipeUnlocker.comp_1932());
			this.add(recipeUnlocker);
		}

		this.counter++;
	}
}
