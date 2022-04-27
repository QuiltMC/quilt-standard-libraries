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

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class ModifyRecipeHandlerImpl implements RecipeLoadingEvents.ModifyRecipesCallback.RecipeHandler {
	final RecipeManager recipeManager;
	final Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
	int counter = 0;

	ModifyRecipeHandlerImpl(RecipeManager recipeManager, Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes) {
		this.recipeManager = recipeManager;
		this.recipes = recipes;
	}

	private void add(Recipe<?> recipe) {
		Map<Identifier, Recipe<?>> type = this.recipes.get(recipe.getType());

		if (type == null) {
			throw new IllegalStateException("The given recipe " + recipe.getId()
					+ " does not have its recipe type " + recipe.getType() + " in the recipe manager.");
		}

		type.put(recipe.getId(), recipe);
	}

	@Override
	public void replace(Recipe<?> recipe) {
		RecipeType<?> oldType = this.getTypeOf(recipe.getId());

		if (oldType == null) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Add new recipe {} with type {} in modify phase.", recipe.getId(), recipe.getType());
			}

			this.add(recipe);
		} else if (oldType == recipe.getType()) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace recipe {} with same type {} in modify phase.", recipe.getId(), recipe.getType());
			}

			this.recipes.get(oldType).put(recipe.getId(), recipe);
		} else {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Replace new recipe {} with type {} (and old type {}) in modify phase.",
						recipe.getId(), recipe.getType(), oldType);
			}

			this.recipes.get(oldType).remove(recipe.getId());
			this.add(recipe);
		}

		this.counter++;
	}

	@Override
	public @Nullable RecipeType<?> getTypeOf(Identifier id) {
		return this.recipes.entrySet().stream()
				.filter(entry -> entry.getValue().containsKey(id))
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	@Override
	public boolean contains(Identifier id) {
		for (var recipes : this.recipes.values()) {
			if (recipes.containsKey(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean contains(Identifier id, RecipeType<?> type) {
		Map<Identifier, Recipe<?>> recipes = this.recipes.get(type);

		if (recipes == null) return false;

		return recipes.containsKey(id);
	}

	@Override
	public @Nullable Recipe<?> getRecipe(Identifier id) {
		for (var recipes : this.recipes.values()) {
			Recipe<?> recipe = recipes.get(id);

			if (recipe != null) {
				return recipe;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> @Nullable T getRecipe(Identifier id, RecipeType<T> type) {
		Map<Identifier, Recipe<?>> recipes = this.recipes.get(type);

		if (recipes == null) return null;

		return (T) recipes.get(id);
	}

	@Override
	public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes() {
		return this.recipes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> Collection<T> getRecipesOfType(RecipeType<T> type) {
		Map<Identifier, Recipe<?>> recipes = this.recipes.get(type);

		if (recipes == null) {
			return ImmutableList.of();
		}

		return (Collection<T>) recipes.values();
	}
}
