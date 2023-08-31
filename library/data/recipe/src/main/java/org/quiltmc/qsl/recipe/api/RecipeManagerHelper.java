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

package org.quiltmc.qsl.recipe.api;

import net.minecraft.recipe.Recipe;

import org.quiltmc.qsl.recipe.impl.RecipeManagerImpl;

/**
 * Represents a helper for the {@link net.minecraft.recipe.RecipeManager}.
 */
public final class RecipeManagerHelper {
	private RecipeManagerHelper() {
		throw new UnsupportedOperationException("RecipeManagerHelper only contains static definitions.");
	}

	/**
	 * Registers a static recipe.
	 * <p>
	 * A static recipe is a recipe that is registered at mod startup (or later) and is kept during the whole lifecycle
	 * of the game.
	 * <p>
	 * Static recipes are automatically added to the {@linkplain net.minecraft.recipe.RecipeManager recipe manager}
	 * when recipes are loaded, and only is added if no other recipe with the same identifier is already loaded.
	 * <p>
	 * Static recipes can be added at any time, but are only applied after a data pack reload.
	 *
	 * @param recipe the recipe to register
	 * @return the registered recipe
	 * @throws IllegalStateException if another recipe with the same identifier is already registered
	 */
	public static Recipe<?> registerStaticRecipe(Recipe<?> recipe) {
		RecipeManagerImpl.registerStaticRecipe(recipe);
		return recipe;
	}

	/**
	 * Registers a dynamic recipe provider.
	 * <p>
	 * The dynamic recipe provider is called when the recipes are loaded.
	 * <p>
	 * Triggered before {@link #modifyRecipes(RecipeLoadingEvents.ModifyRecipesCallback)}
	 * and {@link #removeRecipes(RecipeLoadingEvents.RemoveRecipesCallback)}.
	 *
	 * @param callback the callback to add recipes
	 * @see RecipeLoadingEvents#ADD
	 */
	public static void addRecipes(RecipeLoadingEvents.AddRecipesCallback callback) {
		RecipeLoadingEvents.ADD.register(callback);
	}

	/**
	 * Modifies recipes in the {@link net.minecraft.recipe.RecipeManager} when it is being built.
	 * <p>
	 * Triggered after {@link #addRecipes(RecipeLoadingEvents.AddRecipesCallback)}
	 * and before {@link #removeRecipes(RecipeLoadingEvents.RemoveRecipesCallback)}.
	 *
	 * @param callback the callback to modify recipes
	 * @see RecipeLoadingEvents#MODIFY
	 */
	public static void modifyRecipes(RecipeLoadingEvents.ModifyRecipesCallback callback) {
		RecipeLoadingEvents.MODIFY.register(callback);
	}

	/**
	 * Removes recipes in the {@link net.minecraft.recipe.RecipeManager} when it is being built.
	 * <p>
	 * Triggered after {@link #addRecipes(RecipeLoadingEvents.AddRecipesCallback)}
	 * and {@link #modifyRecipes(RecipeLoadingEvents.ModifyRecipesCallback)}.
	 *
	 * @param callback the callback to remove recipes
	 * @see RecipeLoadingEvents#REMOVE
	 */
	public static void removeRecipes(RecipeLoadingEvents.RemoveRecipesCallback callback) {
		RecipeLoadingEvents.REMOVE.register(callback);
	}
}
