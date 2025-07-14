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
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.RecipeLoadingEvents;

@ApiStatus.Internal
final class RemoveRecipeHandlerImpl extends BasicRecipeHandlerImpl implements
		RecipeLoadingEvents.RemoveRecipesCallback.RecipeHandler {
	int counter = 0;

	RemoveRecipeHandlerImpl(
			RecipeManager recipeManager,
			Multimap<RecipeType<?>, RecipeHolder<?>> byType,
			Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey,
			HolderLookup.Provider registries
	) {
		super(recipeManager, byType, byKey, registries);
	}

	@Override
	public void remove(Identifier id) {
		RecipeType<?> recipeType = this.getTypeOf(id);

		if (recipeType == null) {
			return;
		}

		RegistryKey<Recipe<?>> key = RegistryKey.of(RegistryKeys.RECIPE, id);
		if (this.byType.get(recipeType).removeIf(holder -> holder.id().equals(key))) {
			this.byKey.remove(key);

			if (RecipeManagerImpl.DEBUG_MODE) {
				RecipeManagerImpl.LOGGER.info(
						"Remove recipe {} with type {} in removal phase.",
						id, recipeType
				);
			}

			this.counter++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Recipe<?>> void removeIf(
			RecipeType<T> recipeType, Predicate<RecipeHolder<T>> recipeRemovalPredicate
	) {
		this.removeIfInternal(
				new TypedView<>(this.byType.get(recipeType), holder -> (RecipeHolder<T>) holder),
				recipeRemovalPredicate
		);
	}

	@Override
	public void removeIf(Predicate<RecipeHolder<?>> recipeRemovalPredicate) {
		for (Map.Entry<RecipeType<?>, Collection<RecipeHolder<?>>> entry : this.byType.asMap().entrySet()) {
			this.removeIfInternal(entry.getValue(), recipeRemovalPredicate);
		}
	}

	private <R extends RecipeHolder<?>> void removeIfInternal(
			Iterable<R> typedRecipes, Predicate<R> recipeRemovalPredicate
	) {
		if (typedRecipes == null) {
			return;
		}

		Iterator<R> typedRecipesItr = typedRecipes.iterator();

		while (typedRecipesItr.hasNext()) {
			R entry = typedRecipesItr.next();

			if (recipeRemovalPredicate.test(entry)) {
				if (RecipeManagerImpl.DEBUG_MODE) {
					RecipeManagerImpl.LOGGER.info(
							"Remove recipe matching predicate {} with type {} in removal phase.",
							entry.id(), entry.value().getType()
					);
				}

				this.byKey.remove(entry.id());
				typedRecipesItr.remove();
				this.counter++;
			}
		}
	}

	/**
	 * Provides a typed iterable view of recipe holders.
	 */
	private record TypedView<R extends RecipeHolder<?>>(
			Iterable<RecipeHolder<?>> untyped,
			Function<RecipeHolder<?>, R> cast
	) implements Iterable<R> {
		@Override
		public @NotNull java.util.Iterator<R> iterator() {
			return new Iterator();
		}

		private final class Iterator implements java.util.Iterator<R> {
			private final java.util.Iterator<RecipeHolder<?>> untyped;

			private Iterator() {
				this.untyped = TypedView.this.untyped.iterator();
			}

			@Override
			public boolean hasNext() {
				return this.untyped.hasNext();
			}

			@Override
			public R next() {
				return TypedView.this.cast.apply(this.untyped.next());
			}

			@Override
			public void remove() {
				this.untyped.remove();
			}
		}
	}
}
