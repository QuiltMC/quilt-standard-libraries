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

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.item.TooltipConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_idvlscju;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.CustomItemSetting;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettingsExtensions;

public class QuiltItemSettingsExtensionsTests implements ModInitializer {
	public static final CustomItemSetting<String> CUSTOM_DATA_TEST = CustomItemSetting.create(() -> null);
	public static final String NAMESPACE = "quilt_item_setting_testmod";

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static RegistryKey<Item> createItemKey(String path) {
		return RegistryKey.of(RegistryKeys.ITEM, createId(path));
	}

	public static Item registerItem(String path, Item.Settings settings) {
		return registerItem(path, Item::new, settings);
	}

	public static <I extends Item, S extends Item.Settings> I registerItem(
			String path, Function<S, I> factory, S settings
	) {
		final RegistryKey<Item> key = createItemKey(path);
		settings.key(key);
		return Registry.register(Registries.ITEM, key, factory.apply(settings));
	}

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers an item with a custom item setting that adds some tooltip.
		registerItem(
				"custom_data_item",
				settings -> new Item(settings) {
					// appendTooltip's' deprecation means override-only, don't call
					@SuppressWarnings("deprecation")
					@Override
					public void appendTooltip(
							ItemStack stack, TooltipContext context, C_idvlscju c_idvlscju,
							Consumer<Text> append, TooltipConfig config
					) {
						append.accept(Text.literal(CUSTOM_DATA_TEST.get(stack.getItem())));
					}
				},
				((QuiltItemSettingsExtensions) new Item.Settings())
					.customSetting(CUSTOM_DATA_TEST, "Look at me! I have a custom setting!")
		);
	}
}
