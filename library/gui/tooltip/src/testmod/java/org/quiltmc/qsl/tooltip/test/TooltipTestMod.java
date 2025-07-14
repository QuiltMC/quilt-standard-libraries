/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tooltip.test;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData;

public final class TooltipTestMod implements ModInitializer {
	public static final String NAMESPACE = "quilt_tooltip_testmod";

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	private static void registerItem(String path, Function<Item.Settings, Item> factory) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, createId(path));
		Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().key(key)));
	}

	@Override
	public void onInitialize(ModContainer mod) {
		registerItem("custom_tooltip_item", SimpleCustomTooltipItem::new);
		registerItem("custom_convertible_tooltip_item", ConvertibleTooltipItem::new);
	}

	private static class SimpleCustomTooltipItem extends Item {
		SimpleCustomTooltipItem(Settings settings) {
			super(settings);
		}

		@Override
		public Optional<TooltipData> getTooltipData(ItemStack stack) {
			return Optional.of(new Data(stack.getItem().getTranslationKey()));
		}
	}

	private static class ConvertibleTooltipItem extends Item {
		ConvertibleTooltipItem(Settings settings) {
			super(settings);
		}

		@Override
		public Optional<TooltipData> getTooltipData(ItemStack stack) {
			return Optional.of(new ConvertibleData(stack.getItem().getTranslationKey()));
		}
	}

	public record Data(String message) implements TooltipData { }

	public record ConvertibleData(String message) implements ConvertibleTooltipData {
		@ClientOnly
		@Override
		public TooltipComponent toComponent() {
			return TooltipComponent.of(Text.literal("Converted Tooltip Data").formatted(Formatting.GOLD).asOrderedText());
		}
	}
}
