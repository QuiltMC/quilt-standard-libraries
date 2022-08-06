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

package org.quiltmc.qsl.recipe.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.recipe.api.AbstractBrewingRecipe;
import org.quiltmc.qsl.recipe.api.CustomPotionBrewingRecipe;
import org.quiltmc.qsl.recipe.api.PotionBrewingRecipe;
import org.quiltmc.qsl.recipe.api.PotionItemBrewingRecipe;

@ApiStatus.Internal
public class RecipeImpl implements ModInitializer {
	public static final Identifier BREWING_ID = new Identifier("quilt_recipe", "brewing");
	public static final RecipeType<AbstractBrewingRecipe<?>> BREWING = RecipeType.register(RecipeImpl.BREWING_ID.toString());
	public static final PotionBrewingRecipe.Serializer<PotionBrewingRecipe> POTION_SERIALIZER = new PotionBrewingRecipe.Serializer<>(PotionBrewingRecipe::new);
	public static final CustomPotionBrewingRecipe.Serializer CUSTOM_POTION_SERIALIZER = new CustomPotionBrewingRecipe.Serializer(CustomPotionBrewingRecipe::new);
	public static final PotionItemBrewingRecipe.Serializer POTION_ITEM_SERIALIZER = new PotionItemBrewingRecipe.Serializer(PotionItemBrewingRecipe::new);
	public static final TagKey<Item> POTIONS = TagKey.of(Registry.ITEM_KEY, new Identifier("quilt", "potions"));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "potion_brewing"), POTION_SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "custom_potion_brewing"), CUSTOM_POTION_SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "potion_item_brewing"), POTION_ITEM_SERIALIZER);
	}
}
