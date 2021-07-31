/*
 * Copyright 2021 QuiltMC
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.World;
import org.quiltmc.qsl.item.api.item.v1.CustomItemSetting;
import org.quiltmc.qsl.item.api.item.v1.QuiltItemSettings;

import java.util.concurrent.ThreadLocalRandom;

public class QuiltItemSettingsTests implements ModInitializer {
	public static final CustomItemSetting<String> CUSTOM_DATA_TEST = CustomItemSetting.create(() -> null);
	public static final String MOD_ID = "qsl_items_item_testmod";

	@Override
	public void onInitialize() {
		// Registers an item with a custom equipment slot.
		Item testItem = new Item(new QuiltItemSettings().group(ItemGroup.MISC).equipmentSlot(stack -> EquipmentSlot.CHEST));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_item"), testItem);

		// Registers an item with a custom item setting that adds some tooltip.
		Item testItem2 = new Item(new QuiltItemSettings().group(ItemGroup.MISC).customSetting(CUSTOM_DATA_TEST, "Look at me! I have a custom setting!"));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_item2"), testItem2);

		// Custom recipe remainders

		Item hammerItem = new Item(new QuiltItemSettings().group(ItemGroup.TOOLS).maxDamage(16).damageIfUsedInCrafting());
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "hammer"), hammerItem);

		Item endHammerItem = new Item(new QuiltItemSettings().group(ItemGroup.TOOLS).maxDamage(32).recipeRemainder(((original, inventory, type, world, pos) -> {
			if (world == null || !world.getRegistryKey().equals(World.END)) {
				return ItemStack.EMPTY;
			}

			ItemStack copy = original.copy();
			copy.damage(1, ThreadLocalRandom.current(), null);

			if (copy.getDamage() < copy.getMaxDamage()) {
				return ItemStack.EMPTY;
			}

			return copy;
		})));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "end_hammer"), endHammerItem);
	}
}
