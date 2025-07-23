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
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.BaseRecipeHandler;

class BasicRecipeHandlerImpl implements BaseRecipeHandler {
	final RecipeManager recipeManager;
	final Multimap<RecipeType<?>, RecipeHolder<?>> byType;
	final Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey;

	private final HolderLookup.Provider registries;

	BasicRecipeHandlerImpl(
			RecipeManager recipeManager,
			Multimap<RecipeType<?>, RecipeHolder<?>> byType,
			Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey,
			HolderLookup.Provider registries
	) {
		this.recipeManager = recipeManager;
		this.byType = byType;
		this.byKey = byKey;
		this.registries = registries;
	}

	@Override
	public @Nullable RecipeType<?> getTypeOf(Identifier id) {
		return this.byType.entries().stream()
			.filter(entry -> entry.getValue().id().getValue().equals(id))
			.findFirst()
			.map(Map.Entry::getKey)
			.orElse(null);
	}

	@Override
	public boolean contains(Identifier id) {
		return this.byKey.containsKey(RegistryKey.of(RegistryKeys.RECIPE, id));
	}

	@Override
	public boolean contains(Identifier id, RecipeType<?> type) {
		Collection<RecipeHolder<?>> typedRecipes = this.byType.get(type);

		return typedRecipes.stream().anyMatch(holder -> holder.id().getValue().equals(id));
	}

	@Override
	public @Nullable RecipeHolder<?> getRecipe(Identifier id) {
		return this.byKey.get(RegistryKey.of(RegistryKeys.RECIPE, id));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> @Nullable RecipeHolder<T> getRecipe(Identifier id, RecipeType<T> type) {
		var typedRecipes = this.byType.get(type);

		if (typedRecipes.isEmpty()) {
			return null;
		}

		return (RecipeHolder<T>) typedRecipes.stream()
			.filter(holder -> holder.id().getValue().equals(id))
			.findFirst()
			.orElse(null);
	}

	@Override
	public ImmutableMultimap<RecipeType<?>, RecipeHolder<?>> getRecipes() {
		return ImmutableMultimap.copyOf(this.byType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> Collection<RecipeHolder<T>> getRecipesOfType(RecipeType<T> type) {
		Collection<RecipeHolder<?>> recipes = this.byType.get(type);

		if (recipes.isEmpty()) {
			return Collections.emptyList();
		}

		return recipes.stream().map(recipeHolder -> (RecipeHolder<T>) recipeHolder).toList();
	}

	@Override
	public @NotNull HolderLookup.Provider getRegistries() {
		return this.registries;
	}
}
