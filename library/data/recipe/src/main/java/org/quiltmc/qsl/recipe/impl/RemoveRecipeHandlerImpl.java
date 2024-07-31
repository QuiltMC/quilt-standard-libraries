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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class RemoveRecipeHandlerImpl extends BasicRecipeHandlerImpl implements RecipeLoadingEvents.RemoveRecipesCallback.RecipeHandler {
	int counter = 0;

	RemoveRecipeHandlerImpl(RecipeManager recipeManager, Multimap<RecipeType<?>, RecipeHolder<?>> recipes,
							Map<Identifier, RecipeHolder<?>> globalRecipes, DynamicRegistryManager registryManager) {
		super(recipeManager, recipes, globalRecipes, registryManager);
	}

	@Override
	public void remove(Identifier id) {
		RecipeType<?> recipeType = this.getTypeOf(id);

		if (recipeType == null) {
			return;
		}

		if (this.recipes.get(recipeType).removeIf(holder -> holder.id().equals(id))) {
			this.globalRecipes.remove(id);

			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info("Remove recipe {} with type {} in removal phase.", id, recipeType);
			}

			this.counter++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> void removeIf(RecipeType<T> recipeType, Predicate<RecipeHolder<T>> recipeRemovalPredicate) {
		this.removeIfInternal(
				this.recipes
					.get(recipeType)
					.stream()
					.map(holder -> (RecipeHolder<T>) holder)
					.collect(Collectors.toCollection(ArrayList::new)),
				recipeRemovalPredicate
		);
	}

	@Override
	public void removeIf(Predicate<RecipeHolder<?>> recipeRemovalPredicate) {
		for (var entry : this.getRecipes().asMap().entrySet()) {
			this.removeIfInternal(entry.getValue(), recipeRemovalPredicate);
		}
	}

	private <T extends RecipeHolder<?>> void removeIfInternal(Collection<T> recipeMap, Predicate<T> recipeRemovalPredicate) {
		if (recipeMap == null) return;

		var it = recipeMap.iterator();

		while (it.hasNext()) {
			var entry = it.next();

			if (recipeRemovalPredicate.test(entry)) {
				if (RecipeManagerImpl.DEBUG_MODE) {
					RecipeManagerImpl.LOGGER.info("Remove recipe {} with type {} in removal phase.", entry.id(), entry.value().getType());
				}

				this.globalRecipes.remove(entry.id());
				it.remove();
				this.counter++;
			}
		}
	}
}
