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

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

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
		var testItem = new Item(new QuiltItemSettings().equipmentSlot(EquipmentSlot.CHEST));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "test_item"), testItem);

		// Registers an item with a custom item setting that adds some tooltip.
		var testItem2 = new Item(new QuiltItemSettings().customSetting(CUSTOM_DATA_TEST, "Look at me! I have a custom setting!"));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "test_item2"), testItem2);
	}
}
