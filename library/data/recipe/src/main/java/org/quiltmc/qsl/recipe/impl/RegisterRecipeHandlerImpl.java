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
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

final class RegisterRecipeHandlerImpl implements RecipeLoadingEvents.AddRecipesCallback.RecipeHandler {
	private final Map<Identifier, JsonElement> resourceMap;
	private final Map<RecipeType<?>, ImmutableMap.Builder<Identifier, RecipeHolder<?>>> builderMap;
	private final ImmutableMap.Builder<Identifier, RecipeHolder<?>> globalRecipeMapBuilder;
	private final DynamicRegistryManager registryManager;
	int registered = 0;

	RegisterRecipeHandlerImpl(
			Map<Identifier, JsonElement> resourceMap,
			Map<RecipeType<?>, ImmutableMap.Builder<Identifier, RecipeHolder<?>>> builderMap,
			ImmutableMap.Builder<Identifier, RecipeHolder<?>> globalRecipeMapBuilder,
			DynamicRegistryManager registryManager
	) {
		this.resourceMap = resourceMap;
		this.builderMap = builderMap;
		this.globalRecipeMapBuilder = globalRecipeMapBuilder;
		this.registryManager = registryManager;
	}

	private void register(RecipeHolder<?> recipeHolder) {
		Recipe<?> recipe = recipeHolder.value();
		ImmutableMap.Builder<Identifier, RecipeHolder<?>> recipeBuilder =
				this.builderMap.computeIfAbsent(recipe.getType(), o -> ImmutableMap.builder());
		recipeBuilder.put(recipeHolder.id(), recipeHolder);
		this.globalRecipeMapBuilder.put(recipeHolder.id(), recipeHolder);
		this.registered++;

		if (RecipeManagerImpl.DEBUG_MODE) {
			RecipeManagerImpl.LOGGER.info("Added recipe {} with type {} in register phase.", recipeHolder.id(), recipe.getType());
		}
	}

	void tryRegister(RecipeHolder<?> recipeHolder) {
		if (!this.resourceMap.containsKey(recipeHolder.id())) {
			this.register(recipeHolder);
		}
	}

	@Override
	public void register(Identifier id, Function<Identifier, RecipeHolder<?>> factory) {
		// Add the recipe only if nothing already provides the recipe.
		if (!this.resourceMap.containsKey(id)) {
			var recipeHolder = factory.apply(id);

			if (!id.equals(recipeHolder.id())) {
				throw new IllegalStateException("The recipe " + recipeHolder.id() + " tried to be registered as " + id);
			}

			this.register(recipeHolder);
		}
	}

	@Override
	public @NotNull DynamicRegistryManager getRegistryManager() {
		return this.registryManager;
	}
}
