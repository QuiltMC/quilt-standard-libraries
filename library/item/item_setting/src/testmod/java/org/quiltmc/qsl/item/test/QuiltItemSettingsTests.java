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

package org.quiltmc.qsl.item.test;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.CustomItemSetting;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class QuiltItemSettingsTests implements ModInitializer {
	public static final CustomItemSetting<String> CUSTOM_DATA_TEST = CustomItemSetting.create(() -> null);
	public static final String NAMESPACE = "quilt_item_setting_testmod";

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers an item with a custom equipment slot.
		var testItem = new Item(new QuiltItemSettings().group(ItemGroup.MISC).equipmentSlot(EquipmentSlot.CHEST));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "test_item"), testItem);

		// Registers an item with a custom item setting that adds some tooltip.
		var testItem2 = new Item(new QuiltItemSettings().group(ItemGroup.MISC).customSetting(CUSTOM_DATA_TEST, "Look at me! I have a custom setting!"));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "test_item2"), testItem2);

		// Custom recipe remainders
		Item hammerItem = new Item(new QuiltItemSettings().group(ItemGroup.TOOLS).maxDamage(16).damageIfUsedInCrafting());
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "hammer"), hammerItem);

		Item furnaceInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMELTING) {
				return Items.DIAMOND.getDefaultStack();
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "weird_ore"), furnaceInputRemainder);

		Item smithingInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMITHING) {
				return Items.NETHERITE_INGOT.getDefaultStack();
			}
			return Items.NETHERITE_SCRAP.getDefaultStack();
		}));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "infinite_netherite"), smithingInputRemainder);

		Item loomInputRemainder = new DyeItem(DyeColor.RED, new QuiltItemSettings().group(ItemGroup.MISC).maxDamage(100).recipeRemainder((original, recipe) -> {
			original.setDamage(original.getDamage() + 1);
			return original.getDamage() == original.getMaxDamage() ? Items.RED_DYE.getDefaultStack() : original;
		}));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "infinite_dye"), loomInputRemainder);

		Item cuttingInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.STONECUTTING) {
				return Items.STONE.getDefaultStack();
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "infinite_stone"), cuttingInputRemainder);
	}
}
