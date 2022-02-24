/*
 * Copyright 2022 QuiltMC
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
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
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
		interface RecipeHandler {
			/**
			 * Replaces a recipe in the {@link net.minecraft.recipe.RecipeManager}.
			 *
			 * @param recipe the recipe
			 */
			void replace(Recipe<?> recipe);

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
			 * @return the registered recipes
			 */
			Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();

			/**
			 * Returns all registered recipes of the specified type.
			 *
			 * @param type the recipe type
			 * @param <T>  the type of the recipe
			 * @return all registered recipes of the specified type
			 */
			<T extends Recipe<?>> Collection<T> getRecipesOfType(RecipeType<T> type);
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
		interface RecipeHandler {
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
			 * @return the registered recipes
			 */
			Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();

			/**
			 * Returns all registered recipes of the specified type.
			 *
			 * @param type the recipe type
			 * @param <T>  the type of the recipe
			 * @return all registered recipes of the specified type
			 */
			<T extends Recipe<?>> Collection<T> getRecipesOfType(RecipeType<T> type);
		}
	}
}
