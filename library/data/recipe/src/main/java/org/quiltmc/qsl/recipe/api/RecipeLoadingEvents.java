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

import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Represents the recipe loading events.
 * <p>
 * Triggered when the recipes are being loaded in the {@link net.minecraft.recipe.RecipeManager}.
 * <p>
 * Events are triggered in the following order:
 * <ol>
 *     <li>{@link #ADD}</li>
 *     <li>{@link #MODIFY}</li>
 *     <li>{@link #REMOVE}</li>
 * </ol>
 */
public final class RecipeLoadingEvents {
	/**
	 * Event to add new recipes while the {@link net.minecraft.recipe.RecipeManager} is being built.
	 * <p>
	 * Triggered before {@link #MODIFY} and {@link #REMOVE}.
	 */
	public static final Event<AddRecipesCallback> ADD = Event.create(AddRecipesCallback.class,
			callbacks -> handler -> {
				for (var callback : callbacks) {
					callback.addRecipes(handler);
				}
			});
	/**
	 * Event to modify recipes while the {@link net.minecraft.recipe.RecipeManager} is being built.
	 * <p>
	 * Triggered after {@link #ADD} and before {@link #REMOVE}.
	 */
	public static final Event<ModifyRecipesCallback> MODIFY = Event.create(ModifyRecipesCallback.class,
			callbacks -> handler -> {
				for (var callback : callbacks) {
					callback.modifyRecipes(handler);
				}
			});
	/**
	 * Event to remove recipes while the {@link net.minecraft.recipe.RecipeManager} is being built.
	 * <p>
	 * Triggered after {@link #ADD} and {@link #MODIFY}.
	 */
	public static final Event<RemoveRecipesCallback> REMOVE = Event.create(RemoveRecipesCallback.class,
			callbacks -> handler -> {
				for (var callback : callbacks) {
					callback.removeRecipes(handler);
				}
			});

	private RecipeLoadingEvents() {
		throw new UnsupportedOperationException("RecipeLoadingEvents only contains static definitions.");
	}

	/**
	 * Callback called to register additional recipes when recipes are loaded.
	 */
	@FunctionalInterface
	public interface AddRecipesCallback extends EventAwareListener {
		/**
		 * Called when recipes are loaded.
		 * <p>
		 * {@code handler} is used to add recipes into the {@linkplain net.minecraft.recipe.RecipeManager recipe manager}.
		 *
		 * @param handler the recipe handler
		 */
		void addRecipes(RecipeHandler handler);

		/**
		 * This interface should not be extended by users.
		 */
		@ApiStatus.NonExtendable
		interface RecipeHandler {
			/**
			 * Registers a recipe into the {@link net.minecraft.recipe.RecipeManager}.
			 * <p>
			 * The recipe factory is only called if the recipe is not already present.
			 *
			 * @param id      identifier of the recipe
			 * @param factory the recipe factory
			 */
			void register(Identifier id, Function<Identifier, Recipe<?>> factory);

			/**
			 * {@return the dynamic registry manager}
			 */
			@Contract(pure = true)
			@NotNull DynamicRegistryManager getRegistryManager();
		}
	}

	/**
	 * Callback called to modify or replace recipes after recipes are loaded.
	 */
	@FunctionalInterface
	public interface ModifyRecipesCallback extends EventAwareListener {
		/**
		 * Called after recipes are loaded to modify and replace recipes.
		 *
		 * @param handler the recipe handler
		 */
		void modifyRecipes(RecipeHandler handler);

		/**
		 * This interface should not be extended by users.
		 */
		@ApiStatus.NonExtendable
		interface RecipeHandler extends BaseRecipeHandler {
			/**
			 * Replaces a recipe in the {@link net.minecraft.recipe.RecipeManager}.
			 *
			 * @param recipe the recipe
			 */
			void replace(Recipe<?> recipe);
		}
	}

	/**
	 * Callback called to remove recipes after recipes are loaded.
	 */
	@FunctionalInterface
	public interface RemoveRecipesCallback extends EventAwareListener {
		/**
		 * Called after recipes are loaded to remove recipes.
		 *
		 * @param handler the recipe handler
		 */
		void removeRecipes(RecipeHandler handler);

		/**
		 * This interface should not be extended by users.
		 */
		@ApiStatus.NonExtendable
		interface RecipeHandler extends BaseRecipeHandler {
			/**
			 * Removes a recipe in the {@link net.minecraft.recipe.RecipeManager}.
			 *
			 * @param recipe the recipe identifier
			 */
			void remove(Identifier recipe);

			/**
			 * Removes a recipe if the predicate returns {@code true}.
			 *
			 * @param recipeType             the recipe type of the recipes to conditionally remove
			 * @param recipeRemovalPredicate the recipe removal predicate
			 * @param <T>                    the type of the recipe
			 */
			<T extends Recipe<?>> void removeIf(RecipeType<T> recipeType, Predicate<T> recipeRemovalPredicate);

			/**
			 * Removes a recipe if the predicate returns {@code true}.
			 *
			 * @param recipeRemovalPredicate the recipe removal predicate
			 */
			void removeIf(Predicate<Recipe<?>> recipeRemovalPredicate);
		}
	}
}
