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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.BaseRecipeHandler;

class BasicRecipeHandlerImpl implements BaseRecipeHandler {
	final RecipeManager recipeManager;
	final Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
	final Map<Identifier, Recipe<?>> globalRecipes;
	private final DynamicRegistryManager registryManager;

	BasicRecipeHandlerImpl(RecipeManager recipeManager, Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes,
			Map<Identifier, Recipe<?>> globalRecipes, DynamicRegistryManager registryManager) {
		this.recipeManager = recipeManager;
		this.recipes = recipes;
		this.globalRecipes = globalRecipes;
		this.registryManager = registryManager;
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
		return this.globalRecipes.containsKey(id);
	}

	@Override
	public boolean contains(Identifier id, RecipeType<?> type) {
		Map<Identifier, Recipe<?>> recipes = this.recipes.get(type);

		if (recipes == null) return false;

		return recipes.containsKey(id);
	}

	@Override
	public @Nullable Recipe<?> getRecipe(Identifier id) {
		return this.globalRecipes.get(id);
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
		return Collections.unmodifiableMap(this.recipes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> Collection<T> getRecipesOfType(RecipeType<T> type) {
		Map<Identifier, Recipe<?>> recipes = this.recipes.get(type);

		if (recipes == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableCollection((Collection<T>) recipes.values());
	}

	@Override
	public @NotNull DynamicRegistryManager getRegistryManager() {
		return this.registryManager;
	}
}
