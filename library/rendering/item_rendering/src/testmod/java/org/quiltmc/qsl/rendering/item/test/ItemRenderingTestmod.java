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

package org.quiltmc.qsl.rendering.item.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.rendering.item.api.client.CooldownOverlayRenderer;
import org.quiltmc.qsl.rendering.item.api.client.CountLabelRenderer;
import org.quiltmc.qsl.rendering.item.api.client.ItemBarRenderer;
import org.quiltmc.qsl.rendering.item.test.client.ItemDecorations;
import org.quiltmc.qsl.rendering.item.test.client.cooldown.FlashingCooldownOverlayRenderer;
import org.quiltmc.qsl.rendering.item.test.client.cooldown.HiddenCooldownOverlayRenderer;
import org.quiltmc.qsl.rendering.item.test.client.countlabel.ObfuscatedCountLabelRenderer;
import org.quiltmc.qsl.rendering.item.test.client.itembar.ConstantItemBarRenderer;
import org.quiltmc.qsl.rendering.item.test.client.itembar.DiscoItemBarRenderer;
import org.quiltmc.qsl.rendering.item.test.client.itembar.EnergyItemBarRenderer;
import org.quiltmc.qsl.rendering.item.test.client.itembar.ManaItemBarRenderer;

public class ItemRenderingTestmod implements ModInitializer {
	public static final String NAMESPACE = "quilt_item_rendering_testmod";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static Item.Settings itemSettings() {
		return new Item.Settings().group(ItemGroup.MISC);
	}

	public static final Item OBFUSCATED_COUNT = new Item(itemSettings()) {
		@Override
		@Environment(EnvType.CLIENT)
		public CountLabelRenderer getCountLabelRenderer() {
			return ObfuscatedCountLabelRenderer.INSTANCE;
		}
	};

	public static final Item ENERGY_STORAGE = new StorageItem(itemSettings()) {
		@Override
		@Environment(EnvType.CLIENT)
		public ItemBarRenderer[] getItemBarRenderers() {
			return new ItemBarRenderer[] { EnergyItemBarRenderer.INSTANCE };
		}
	};

	public static final Item MANA_STORAGE = new StorageItem(itemSettings()) {
		@Override
		@Environment(EnvType.CLIENT)
		public ItemBarRenderer[] getItemBarRenderers() {
			return new ItemBarRenderer[] { ManaItemBarRenderer.INSTANCE };
		}
	};

	public static final Item DISCO_BALL = new Item(itemSettings()) {
		@Override
		@Environment(EnvType.CLIENT)
		public ItemBarRenderer[] getItemBarRenderers() {
			return new ItemBarRenderer[] { DiscoItemBarRenderer.INSTANCE };
		}
	};

	public static final Item MULTI_BAR = new Item(itemSettings()) {
		@Override
		@Environment(EnvType.CLIENT)
		public ItemBarRenderer[] getItemBarRenderers() {
			return new ItemBarRenderer[] {
					new ConstantItemBarRenderer(5, 0xFFFF0000),
					new ConstantItemBarRenderer(9, 0xFF00FF00),
					new ConstantItemBarRenderer(2, 0xFF0000FF)
			};
		}
	};

	public static final Item LONG_COOLDOWN = new CooldownItem(itemSettings(), 4 * 20) {
		@Override
		@Environment(EnvType.CLIENT)
		public CooldownOverlayRenderer getCooldownOverlayRenderer() {
			return FlashingCooldownOverlayRenderer.INSTANCE;
		}
	};

	public static final Item HIDDEN_COOLDOWN = new CooldownItem(itemSettings(), 4 * 20) {
		@Override
		@Environment(EnvType.CLIENT)
		public CooldownOverlayRenderer getCooldownOverlayRenderer() {
			return HiddenCooldownOverlayRenderer.INSTANCE;
		}
	};

	public static final Item TUNISIAN_DIAMOND = new Item(itemSettings()) {
		@Override
		public boolean preRenderOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
			ItemDecorations.renderStackBorder(matrices, Formatting.GOLD);
			return true;
		}
	};

	public static final Item MYSTERIOUS_BOOK = new Item(itemSettings()) {
		@Override
		public boolean preRenderOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
			ItemDecorations.renderStackBorder(matrices, Formatting.DARK_PURPLE);
			return true;
		}

		@Override
		public void postRenderOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
			ItemDecorations.renderWarningIcon(matrices);
		}
	};

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ITEM, id("obfuscated_count"), OBFUSCATED_COUNT);
		Registry.register(Registry.ITEM, id("energy_storage"), ENERGY_STORAGE);
		Registry.register(Registry.ITEM, id("mana_storage"), MANA_STORAGE);
		Registry.register(Registry.ITEM, id("disco_ball"), DISCO_BALL);
		Registry.register(Registry.ITEM, id("multi_bar"), MULTI_BAR);
		Registry.register(Registry.ITEM, id("long_cooldown"), LONG_COOLDOWN);
		Registry.register(Registry.ITEM, id("hidden_cooldown"), HIDDEN_COOLDOWN);
		Registry.register(Registry.ITEM, id("tunisian_diamond"), TUNISIAN_DIAMOND);
		Registry.register(Registry.ITEM, id("mysterious_book"), MYSTERIOUS_BOOK);
	}
}
