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
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class RemoveRecipeHandlerImpl implements RecipeLoadingEvents.RemoveRecipesCallback.RecipeHandler {
	final RecipeManager recipeManager;
	final Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
	int counter = 0;

	RemoveRecipeHandlerImpl(RecipeManager recipeManager, Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes) {
		this.recipeManager = recipeManager;
		this.recipes = recipes;
	}

	@Override
	public void remove(Identifier id) {
		RecipeType<?> recipeType = this.getTypeOf(id);

		if (recipeType == null) {
			return;
		}

		if (this.recipes.get(recipeType).remove(id) != null) {
			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Remove recipe {} with type {} in removal phase.", id, recipeType);
			}

			this.counter++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> void removeIf(RecipeType<T> recipeType, Predicate<T> recipeRemovalPredicate) {
		this.removeIf((Map<Identifier, T>) this.recipes.get(recipeType), recipeRemovalPredicate);
	}

	@Override
	public void removeIf(Predicate<Recipe<?>> recipeRemovalPredicate) {
		for (var entry : this.getRecipes().entrySet()) {
			this.removeIf(entry.getValue(), recipeRemovalPredicate);
		}
	}

	protected <T extends Recipe<?>> void removeIf(Map<Identifier, T> recipeMap, Predicate<T> recipeRemovalPredicate) {
		if (recipeMap == null) return;

		var it = recipeMap.entrySet().iterator();

		while (it.hasNext()) {
			var entry = it.next();

			if (recipeRemovalPredicate.test(entry.getValue())) {
				if (RecipeManagerImpl.DEBUG_MODE) {
					RecipeManagerImpl.LOGGER.info("Remove recipe {} with type {} in removal phase.", entry.getKey(), entry.getValue().getType());
				}

				it.remove();
				this.counter++;
			}
		}
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
