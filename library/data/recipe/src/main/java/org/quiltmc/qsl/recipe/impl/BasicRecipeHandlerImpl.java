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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.BaseRecipeHandler;

class BasicRecipeHandlerImpl implements BaseRecipeHandler {
	final RecipeManager recipeManager;
	final Multimap<RecipeType<?>, RecipeHolder<?>> recipes;
	final Map<Identifier, RecipeHolder<?>> globalRecipes;
	private final DynamicRegistryManager registryManager;

	BasicRecipeHandlerImpl(RecipeManager recipeManager, Multimap<RecipeType<?>, RecipeHolder<?>> recipes,
						   Map<Identifier, RecipeHolder<?>> globalRecipes, DynamicRegistryManager registryManager) {
		this.recipeManager = recipeManager;
		this.recipes = recipes;
		this.globalRecipes = globalRecipes;
		this.registryManager = registryManager;
	}

	@Override
	public @Nullable RecipeType<?> getTypeOf(Identifier id) {
		return recipes.entries().stream()
			.filter(entry -> entry.getValue().id().equals(id))
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
		Collection<RecipeHolder<?>> recipe = this.recipes.get(type);

		if (recipe.isEmpty()) return false;

		return recipe.stream().anyMatch(holder -> holder.id().equals(id));
	}

	@Override
	public @Nullable RecipeHolder<?> getRecipe(Identifier id) {
		return this.globalRecipes.get(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> @Nullable RecipeHolder<T> getRecipe(Identifier id, RecipeType<T> type) {
		Collection<RecipeHolder<?>> recipes = this.recipes.get(type);

		if (recipes.isEmpty()) return null;

		return (RecipeHolder<T>) recipes.stream().filter(holder -> holder.id().equals(id)).findFirst().orElse(null);
	}

	@Override
	public ImmutableMultimap<RecipeType<?>, RecipeHolder<?>> getRecipes() {
		return ImmutableMultimap.copyOf(this.recipes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> Collection<RecipeHolder<T>> getRecipesOfType(RecipeType<T> type) {
		Collection<RecipeHolder<?>> recipes = this.recipes.get(type);

		if (recipes.isEmpty()) {
			return Collections.emptyList();
		}

		return recipes.stream().map(recipeHolder -> (RecipeHolder<T>) recipeHolder).toList();
	}

	@Override
	public @NotNull DynamicRegistryManager getRegistryManager() {
		return this.registryManager;
	}
}
