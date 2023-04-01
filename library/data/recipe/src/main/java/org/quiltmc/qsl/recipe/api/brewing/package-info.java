/*
 * Copyright 2023 QuiltMC
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
 * <h2>The Brewing Recipe API.</h2>
 * <p>
 * <h3>What does this API do?</h3>
 * The brewing Recipe API allows users to define data-driven recipes for the brewing stand. By default, there are three
 * different brewing recipe types:
 * <ul>
 *     <li>{@link org.quiltmc.qsl.recipe.api.brewing.PotionItemBrewingRecipe PotionItemBrewingRecipe}</li>
 *     <li>{@link org.quiltmc.qsl.recipe.api.brewing.SimplePotionBrewingRecipe SimplePotionBrewingRecipe}</li>
 *     <li>{@link org.quiltmc.qsl.recipe.api.brewing.CustomPotionBrewingRecipe CustomPotionBrewingRecipe}</li>
 * </ul>
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe AbstractBrewingRecipe}</h3>
 * The {@link org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe AbstractBrewingRecipe} represents the shared
 * attributes across all brewing recipes. Any new types of brewing recipes should extend
 * {@link org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe AbstractBrewingRecipe}.
 * <p>
 * All brewing recipes share five arguments:
 * <ul>
 * 		<li>type: An {@link net.minecraft.util.Identifier identifier} matching the registered recipe serializer</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid {@link net.minecraft.recipe.Ingredient ingredient} JSON object.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies {@code 20} fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is {@code 400} ticks.</li>
 * </ul>
 * Additionally, the {@code input} and {@code output} fields are usually {@link net.minecraft.util.Identifier identifiers} for their
 * respective type.
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.brewing.PotionItemBrewingRecipe PotionItemBrewingRecipe}</h3>
 * This recipe is for transforming one {@link net.minecraft.item.PotionItem PotionItem} into another. For example, a
 * {@link net.minecraft.item.Items#POTION potion} can be transformed into a {@link net.minecraft.item.Items#SPLASH_POTION splash potion}
 * using {@link net.minecraft.item.Items#GUNPOWDER gunpowder}.
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.brewing.SimplePotionBrewingRecipe SimplePotionBrewingRecipe}</h3>
 * This recipe is for transforming the {@link net.minecraft.potion.Potion Potion} of an {@link net.minecraft.item.ItemStack}.
 * <p>
 * <h3>{@link org.quiltmc.qsl.recipe.api.brewing.CustomPotionBrewingRecipe CustomPotionBrewingRecipe}</h3>
 * This recipe is an extension of the {@link org.quiltmc.qsl.recipe.api.brewing.SimplePotionBrewingRecipe SimplePotionBrewingRecipe},
 * with added support for custom potion effects. Custom potion effects can be added through the {@code "effects"} entry.
 * Each entry is either a valid {@link net.minecraft.util.Identifier identifier} for a {@link net.minecraft.entity.effect.StatusEffect status effect} or a JSON object of the form:
 * <ul>
 *     <li>type: A valid {@link net.minecraft.util.Identifier identifier} for a {@link net.minecraft.entity.effect.StatusEffect status effect}.</li>
 *     <li>duration: An integer representing how long this effect lasts, in ticks.</li>
 *     <li>amplifier: An integer representing what level this effect is. Note that this is zero-based.</li>
 *     <li>particles: {@code true} if the effect should produce particles, or {@code false}.</li>
 *     <li>icon: {@code true} if the effect should show an icon in the HUD, or {@code false}.</li>
 * </ul>
 * <p>
 * QuiltRecipeTypes can also be built at runtime via the {@link org.quiltmc.qsl.recipe.api.builder.QuiltRecipeBuilders QuiltRecipeBuilders} and the {@link org.quiltmc.qsl.recipe.api Recipe API}.
 */

package org.quiltmc.qsl.recipe.api.brewing;
