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
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

final class RegisterRecipeHandlerImpl implements RecipeLoadingEvents.AddRecipesCallback.RecipeHandler {
	private final Map<Identifier, JsonElement> resourceMap;
	private final Map<RecipeType<?>, ImmutableMap.Builder<Identifier, RecipeUnlocker<?>>> builderMap;
	private final ImmutableMap.Builder<Identifier, RecipeUnlocker<?>> globalRecipeMapBuilder;
	private final DynamicRegistryManager registryManager;
	int registered = 0;

	RegisterRecipeHandlerImpl(
			Map<Identifier, JsonElement> resourceMap,
			Map<RecipeType<?>, ImmutableMap.Builder<Identifier, RecipeUnlocker<?>>> builderMap,
			ImmutableMap.Builder<Identifier, RecipeUnlocker<?>> globalRecipeMapBuilder,
			DynamicRegistryManager registryManager
	) {
		this.resourceMap = resourceMap;
		this.builderMap = builderMap;
		this.globalRecipeMapBuilder = globalRecipeMapBuilder;
		this.registryManager = registryManager;
	}

	private void register(RecipeUnlocker<?> recipeUnlocker) {
		Recipe<?> recipe = recipeUnlocker.comp_1933();
		ImmutableMap.Builder<Identifier, RecipeUnlocker<?>> recipeBuilder =
				this.builderMap.computeIfAbsent(recipe.getType(), o -> ImmutableMap.builder());
		recipeBuilder.put(recipeUnlocker.comp_1932(), recipeUnlocker);
		this.globalRecipeMapBuilder.put(recipeUnlocker.comp_1932(), recipeUnlocker);
		this.registered++;

		if (RecipeManagerImpl.DEBUG_MODE) {
			RecipeManagerImpl.LOGGER.info("Added recipe {} with type {} in register phase.", recipeUnlocker.comp_1932(), recipe.getType());
		}
	}

	void tryRegister(RecipeUnlocker<?> recipeUnlocker) {
		if (!this.resourceMap.containsKey(recipeUnlocker.comp_1932())) {
			this.register(recipeUnlocker);
		}
	}

	@Override
	public void register(Identifier id, Function<Identifier, RecipeUnlocker<?>> factory) {
		// Add the recipe only if nothing already provides the recipe.
		if (!this.resourceMap.containsKey(id)) {
			var recipeUnlocker = factory.apply(id);

			if (!id.equals(recipeUnlocker.comp_1932())) {
				throw new IllegalStateException("The recipe " + recipeUnlocker.comp_1932() + " tried to be registered as " + id);
			}

			this.register(recipeUnlocker);
		}
	}

	@Override
	public @NotNull DynamicRegistryManager getRegistryManager() {
		return this.registryManager;
	}
}
