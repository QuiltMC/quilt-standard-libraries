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

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;
import org.quiltmc.qsl.recipe.api.data.RecipeData;

@ApiStatus.Internal
final class ModifyRecipeHandlerImpl extends BasicRecipeHandlerImpl implements
		RecipeLoadingEvents.ModifyRecipesCallback.RecipeHandler {
	int counter = 0;

	ModifyRecipeHandlerImpl(
			RecipeManager recipeManager,
			Multimap<RecipeType<?>, RecipeHolder<?>> byType,
			Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey,
			HolderLookup.Provider registries
	) {
		super(recipeManager, byType, byKey, registries);
	}

	@Override
	public void replace(Identifier id, RecipeData<?, ?> recipe) {
		recipe.createRecipe(this.getRegistries())
				.resultOrPartial(error ->
					RecipeManagerImpl.LOGGER.error("Error creating replacement recipe {}: [{}]", id, error)
				)
				.ifPresent(newRecipe -> {
					RecipeHolder<?> oldRecipeHolder = this.getRecipe(id);

					if (oldRecipeHolder == null) {
						if (RecipeManagerImpl.DEBUG_MODE) {
							RecipeManagerImpl.LOGGER.info(
									"Add new recipe {} with type {} in modify phase.",
									id, newRecipe.getType()
							);
						}
					} else {
						RecipeType<?> oldType = oldRecipeHolder.value().getType();

						if (RecipeManagerImpl.DEBUG_MODE) {
							if (oldType == newRecipe.getType()) {
								RecipeManagerImpl.LOGGER.info(
										"Replace recipe {} with same type {} in modify phase.",
										id, newRecipe.getType()
								);
							} else {
								RecipeManagerImpl.LOGGER.info(
										"Replace new recipe {} with type {} (and old type {}) in modify phase.",
										id, newRecipe.getType(), oldType
								);
							}
						}

						this.byType.get(oldType).remove(oldRecipeHolder);
					}

					RecipeType<? extends Recipe<?>> type = newRecipe.getType();

					if (!this.byType.containsKey(type)) {
						throw new IllegalStateException(
							"The given recipe %s does not have its recipe type %s in the recipe manager."
								.formatted(id, type)
						);
					}

					RecipeHolder<Recipe<?>> recipeHolder =
						new RecipeHolder<>(RegistryKey.of(RegistryKeys.RECIPE, id), newRecipe);
					this.byType.get(type).add(recipeHolder);
					this.byKey.put(recipeHolder.id(), recipeHolder);

					this.counter++;
				});
	}
}
