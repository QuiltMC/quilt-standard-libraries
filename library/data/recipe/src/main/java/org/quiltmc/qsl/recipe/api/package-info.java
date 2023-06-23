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

/**
 * <h2>The Recipe API.</h2>
 * <p>
 * <h3>Quick note about vocabulary in the Recipe API:</h3>
 * <ul>
 *     <li>A static recipe is a recipe which is registered with the API once and is automatically added to
 *     the {@linkplain net.minecraft.recipe.RecipeManager recipe manager} when recipes are loaded.</li>
 *     <li>A dynamic recipe is a recipe which is registered when recipes are loaded.</li>
 * </ul>
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.RecipeManagerHelper RecipeManagerHelper}</h3>
 * The {@link org.quiltmc.qsl.recipe.api.RecipeManagerHelper RecipeManagerHelper} is a helper class focused
 * on the {@link net.minecraft.recipe.RecipeManager}, it allows you to register static and dynamic recipes,
 * it also allows you to modify, replace, and remove recipes.
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.RecipeLoadingEvents RecipeLoadingEvents}</h3>
 * Contains some events to register, modify, and remove recipes.
 * <p>
 * <h3>When to use the Recipe API?</h3>
 * This API targets specific use-cases:
 * <ul>
 *     <li>Dynamic recipe registration, in the case the recipes cannot be determined at compile-time.</li>
 *     <li>Modification and removal of recipes.</li>
 * </ul>
 * This API is <b>NOT</b> supposed to be used for known at compile-time data-generation, please use the appropriate tools instead.
 */

package org.quiltmc.qsl.recipe.api;
