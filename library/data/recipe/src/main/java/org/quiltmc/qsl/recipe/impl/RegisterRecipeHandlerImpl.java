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
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;
import org.quiltmc.qsl.recipe.api.data.RecipeData;

final class RegisterRecipeHandlerImpl implements RecipeLoadingEvents.AddRecipesCallback.RecipeHandler {
	private final Map<Identifier, Recipe<?>> resourceMap;
	private final ImmutableList.Builder<RecipeHolder<?>> recipes;
	private final HolderLookup.Provider registries;
	int registered = 0;

	RegisterRecipeHandlerImpl(
			Map<Identifier, Recipe<?>> resourceMap,
			HolderLookup.Provider registries
	) {
		this.resourceMap = resourceMap;
		this.recipes = ImmutableList.builder();
		this.registries = registries;
	}

	private void register(RecipeHolder<?> recipeHolder) {
		this.recipes.add(recipeHolder);
		this.registered++;

		if (RecipeManagerImpl.DEBUG_MODE) {
			RecipeManagerImpl.LOGGER.info(
					"Added recipe {} with type {} in register phase.",
					recipeHolder.id(), recipeHolder.value().getType()
			);
		}
	}

	void tryRegister(RecipeHolder<?> recipeHolder) {
		if (!this.resourceMap.containsKey(recipeHolder.id().getValue())) {
			this.register(recipeHolder);
		}
	}

	@Override
	public void register(Identifier id, Function<Identifier, RecipeData<?, ?>> factory) {
		// Add the recipe only if nothing already provides the recipe.
		if (!this.resourceMap.containsKey(id)) {
			factory.apply(id).createRecipe(this.registries)
				.resultOrPartial(error -> RecipeManagerImpl.LOGGER.error("Error creating recipe {}: [{}]", id, error))
				.map(recipe -> new RecipeHolder<>(RegistryKey.of(RegistryKeys.RECIPE, id), recipe))
				.ifPresent(this::register);
		}
	}

	@Override
	public @NotNull HolderLookup.Provider getRegistries() {
		return this.registries;
	}

	public Collection<RecipeHolder<?>> buildRecipes() {
		return this.recipes.build();
	}
}
