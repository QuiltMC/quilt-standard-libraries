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

package org.quiltmc.qsl.item.test;

import static org.quiltmc.qsl.item.test.QuiltItemSettingsExtensionsTests.registerItem;

import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DyeColor;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettingsExtensions;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;

public class RecipeRemainderTests implements ModInitializer {
	// Static field so we can use it in BrewingRecipeRegistryMixin
	public static final Item POTION_INGREDIENT_REMAINDER = registerItem(
			"potion_ingredient_remainder",
			((QuiltItemSettingsExtensions) new Item.Settings())
				.recipeRemainder((original, recipe) ->
					new ItemStack(Items.BLAZE_POWDER), RecipeRemainderLocation.POTION_ADDITION
				)
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// TODO: figure out a way to test these better. Maybe a gametest?
		registerItem(
				"hammer",
				((QuiltItemSettingsExtensions) new Item.Settings()
					.maxDamage(16))
					.recipeDamageRemainder()
		);

		// furnace input remainder
		registerItem(
				"weird_ore",
				((QuiltItemSettingsExtensions) new Item.Settings())
				.recipeRemainder(
					(original, recipe) -> Items.DIAMOND.getDefaultStack(),
					RecipeRemainderLocation.FURNACE_INGREDIENT
				)
		);

		// furnace input self remainder
		registerItem(
				"infinite_ore",
				((QuiltItemSettingsExtensions) new Item.Settings())
					.recipeRemainder(
						(original, recipe) -> {
							ItemStack copy = original.copy();
							copy.setCount(2);
							return copy;
						},
						RecipeRemainderLocation.FURNACE_INGREDIENT
					)
		);

		// furnace fuel self remainder
		registerItem(
				"infinite_fuel",
				((QuiltItemSettingsExtensions) new Item.Settings())
					.recipeRemainder(
						(original, recipe) -> {
							ItemStack remainder = original.copy();
							if (recipe != null) {
								if (recipe.getType() == RecipeType.SMELTING) {
									remainder.setCount(1);
								} else if (recipe.getType() == RecipeType.SMOKING) {
									remainder.setCount(2);
								} else if (recipe.getType() == RecipeType.BLASTING) {
									remainder.setCount(3);
								}
							}

							return remainder;
						},
						RecipeRemainderLocation.FURNACE_FUEL
					)
		);

		// smithing input remainder
		registerItem(
				"infinite_netherite",
				((QuiltItemSettingsExtensions) new Item.Settings())
					.recipeSelfRemainder(RecipeRemainderLocation.SMITHING_INGREDIENT)
		);

		// smithing template remainder
		registerItem(
				"reusable_netherite_template",
				((QuiltItemSettingsExtensions) new Item.Settings()
					.maxDamage(100))
					.recipeDamageRemainder(1, RecipeRemainderLocation.SMITHING_TEMPLATE)
		);

		// smithing base remainder
		registerItem(
				"leaving_leather_base",
				((QuiltItemSettingsExtensions) new Item.Settings())
					.recipeRemainder((original, recipe) ->
						new ItemStack(Items.LEATHER), RecipeRemainderLocation.SMITHING_BASE
					)
		);

		// loom input remainder
		registerItem(
				"reusable_dye",
				settings -> new DyeItem(DyeColor.RED, settings),
				((QuiltItemSettingsExtensions) new Item.Settings()
					.maxDamage(100))
					.recipeDamageRemainder(RecipeRemainderLocation.LOOM_DYE)
		);

		// cutting input remainder
		registerItem(
				"infinite_stone",
				((QuiltItemSettingsExtensions) new Item.Settings())
					.recipeRemainder(
						(original, recipe) -> Items.STONE.getDefaultStack(),
						RecipeRemainderLocation.STONECUTTER_INPUT
					)
		);
	}
}
