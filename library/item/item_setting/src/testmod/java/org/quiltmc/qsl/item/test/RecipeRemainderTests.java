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

import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class RecipeRemainderTests implements ModInitializer {
	// Static field so we can use it in BrewingRecipeRegistryMixin
	public static final Item POTION_INGREDIENT_REMAINDER = Registry.register(
			Registries.ITEM,
			new Identifier(QuiltItemSettingsTests.NAMESPACE, "potion_ingredient_remainder"),
			new Item(
					new QuiltItemSettings().recipeRemainder(
							(original, recipe) -> new ItemStack(Items.BLAZE_POWDER)
					)
			)
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Item hammerItem = new Item(new QuiltItemSettings().maxDamage(16).recipeDamageRemainder());
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "hammer"), hammerItem);

		Item furnaceInputRemainder = new Item(new QuiltItemSettings().recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMELTING) {
				return Items.DIAMOND.getDefaultStack();
			}

			return ItemStack.EMPTY;
		}));
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "weird_ore"), furnaceInputRemainder);

		Item furnaceInputSelfRemainder = new Item(new QuiltItemSettings().recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMELTING) {
				var remainder = original.copy();
				remainder.setCount(2);
				return remainder;
			}

			return ItemStack.EMPTY;
		}));
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_ore"), furnaceInputSelfRemainder);

		Item furnaceFuelSelfRemainder = new Item(new QuiltItemSettings().recipeRemainder((original, recipe) -> {
			var remainder = original.copy();
			if (recipe != null) {
				if (recipe.getType() == RecipeType.SMELTING) {
					remainder.setCount(1);
				} else if (recipe.getType() == RecipeType.SMOKING) {
					remainder.setCount(2);
				} else if (recipe.getType() == RecipeType.BLASTING) {
					remainder.setCount(3);
				}

				return remainder;
			}

			return ItemStack.EMPTY;
		}));
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_fuel"), furnaceFuelSelfRemainder);

		Item smithingInputRemainder = new Item(new QuiltItemSettings().recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMITHING) {
				return original.getItem().getDefaultStack();
			}

			return Items.NETHERITE_SCRAP.getDefaultStack();
		}));
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_netherite"), smithingInputRemainder);

		Item loomInputRemainder = new DyeItem(DyeColor.RED, new QuiltItemSettings().maxDamage(100).recipeDamageRemainder());
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_dye"), loomInputRemainder);

		Item cuttingInputRemainder = new Item(new QuiltItemSettings().recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.STONECUTTING) {
				return Items.STONE.getDefaultStack();
			}

			return ItemStack.EMPTY;
		}));
		Registry.register(Registries.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_stone"), cuttingInputRemainder);
	}
}
