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

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

/**
 * Represents common recipe handler methods.
 */
@ApiStatus.NonExtendable
public interface BaseRecipeHandler {
	/**
	 * Returns the recipe type of the specified recipe.
	 *
	 * @param id the identifier of the recipe
	 * @return the recipe type if the recipe is present, else {@code null}
	 */
	@Nullable RecipeType<?> getTypeOf(Identifier id);

	/**
	 * Returns whether the {@link net.minecraft.recipe.RecipeManager} contains the specified recipe.
	 *
	 * @param id the identifier of the recipe
	 * @return {@code true} if the recipe is present in the {@link net.minecraft.recipe.RecipeManager}, else {@code false}
	 */
	boolean contains(Identifier id);

	/**
	 * Returns whether the {@link net.minecraft.recipe.RecipeManager} contains the specified recipe of the specified recipe type.
	 *
	 * @param id   the identifier of the recipe
	 * @param type the type of the recipe
	 * @return {@code true} if the recipe is present in the {@link net.minecraft.recipe.RecipeManager}, else {@code false}
	 */
	boolean contains(Identifier id, RecipeType<?> type);

	/**
	 * Returns the recipe in {@link net.minecraft.recipe.RecipeManager} from its identifier.
	 *
	 * @param id the identifier of the recipe
	 * @return the recipe if present, else {@code null}
	 */
	@Nullable Recipe<?> getRecipe(Identifier id);

	/**
	 * Returns the recipe of the specified recipe type in {@link net.minecraft.recipe.RecipeManager} from its identifier.
	 *
	 * @param id   the identifier of the recipe
	 * @param type the type of the recipe
	 * @param <T>  the type of the recipe
	 * @return the recipe if present and of the correct type, else {@code null}
	 */
	@Nullable <T extends Recipe<?>> T getRecipe(Identifier id, RecipeType<T> type);

	/**
	 * Returns all registered recipes.
	 *
	 * @return a view of the registered recipes
	 */
	Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();

	/**
	 * Returns all registered recipes of the specified type.
	 *
	 * @param type the recipe type
	 * @param <T>  the type of the recipe
	 * @return a view of all the registered recipes of the specified type
	 */
	<T extends Recipe<?>> Collection<T> getRecipesOfType(RecipeType<T> type);

	/**
	 * {@return the dynamic registry manager}
	 */
	@Contract(pure = true)
	@NotNull DynamicRegistryManager getRegistryManager();
}
