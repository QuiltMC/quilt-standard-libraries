/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.recipe.api.data;

import com.mojang.serialization.DataResult;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeInput;
import net.minecraft.registry.HolderLookup;

/**
 * Represents the data required to create a recipe.
 *
 * <p>Unlike {@link Recipe}s and {@link RecipeHolder}s, {@code RecipeData} instances are safe to create before dynamic
 * content is loaded (e.g. during mod initialization) because dynamic content (e.g. tags) isn't accessed until recipes
 * are loaded.<br>
 * {@code RecipeData} implementations should be immutable.
 *
 * @param <I> the input type of the recipe this data represents
 * @param <R> the type of the recipe this data represents
 */
public interface RecipeData<I extends RecipeInput, R extends Recipe<I>> {
	/**
	 * Creates the recipe this data represents.
	 *
	 * <p>This is called whenever recipes are re/loaded.<br>
	 * It should create a new recipe instance each time it's called to ensure that any changes in dynamic content are
	 * reflected in the recipe.
	 *
	 * @param registries access to the game's registries; provides safe access to dynamic content
	 *
	 * @return the result of attempting to create the recipe this data represents;
	 * may be an {@linkplain DataResult.Error error} if required content is missing
	 */
	DataResult<R> createRecipe(HolderLookup.Provider registries);
}
