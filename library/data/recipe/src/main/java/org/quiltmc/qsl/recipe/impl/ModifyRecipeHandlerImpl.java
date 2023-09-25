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

import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class ModifyRecipeHandlerImpl extends BasicRecipeHandlerImpl implements RecipeLoadingEvents.ModifyRecipesCallback.RecipeHandler {
	int counter = 0;

	ModifyRecipeHandlerImpl(RecipeManager recipeManager, Map<RecipeType<?>, Map<Identifier, RecipeHolder<?>>> recipes,
			Map<Identifier, RecipeHolder<?>> globalRecipes, DynamicRegistryManager registryManager) {
		super(recipeManager, recipes, globalRecipes, registryManager);
	}

	private void add(RecipeHolder<?> recipeHolder) {
		Map<Identifier, RecipeHolder<?>> type = this.recipes.get(recipeHolder.value().getType());

		if (type == null) {
			throw new IllegalStateException("The given recipe " + recipeHolder.id()
					+ " does not have its recipe type " + type + " in the recipe manager.");
		}

		type.put(recipeHolder.id(), recipeHolder);
		this.globalRecipes.put(recipeHolder.id(), recipeHolder);
	}

	@Override
	public void replace(RecipeHolder<?> recipeHolder) {
		RecipeType<?> oldType = this.getTypeOf(recipeHolder.id());

		if (oldType == null) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Add new recipe {} with type {} in modify phase.", recipeHolder.id(), recipeHolder.value().getType());
			}

			this.add(recipeHolder);
		} else if (oldType == recipeHolder.value().getType()) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace recipe {} with same type {} in modify phase.", recipeHolder.id(), recipeHolder.value().getType());
			}

			this.recipes.get(oldType).put(recipeHolder.id(), recipeHolder);
			this.globalRecipes.put(recipeHolder.id(), recipeHolder);
		} else {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace new recipe {} with type {} (and old type {}) in modify phase.", recipeHolder.id(), recipeHolder.value().getType(), oldType);
			}

			this.recipes.get(oldType).remove(recipeHolder.id());
			this.add(recipeHolder);
		}

		this.counter++;
	}
}
