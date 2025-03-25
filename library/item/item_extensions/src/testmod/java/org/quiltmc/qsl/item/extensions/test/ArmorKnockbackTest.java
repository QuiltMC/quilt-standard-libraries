/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.item.extensions.test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


import net.minecraft.client.item.TooltipConfig;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_cebksdnb;
import net.minecraft.util.*;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ArmorKnockbackTest implements ModInitializer {
	private static final ArmorMaterial KNOCKBACK_RESISTANCE_ARMOR = new ArmorMaterial(
		1000,
		Map.of(
			C_cebksdnb.BOOTS, 0,
			C_cebksdnb.LEGGINGS, 0,
			C_cebksdnb.CHESTPLATE, 0,
			C_cebksdnb.HELMET, 0,
			C_cebksdnb.BODY, 0
		),
		0,
		Registries.SOUND_EVENT.wrapAsHolder(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME),
		0.0f,
		200.0F,
		ItemTags.WOOL,
		RegistryKey.of(EquipmentAssets.REGISTRY, Identifier.of("quilt-item-extension-testmod", "knockback_armor")));

	private static final ArmorItem KNOCKBACK_RESISTANCE_CHESTPLATE = new ArmorItem(
			KNOCKBACK_RESISTANCE_ARMOR,
			C_cebksdnb.CHESTPLATE,
			new Item.Settings().rarity(Rarity.RARE)
	) {
		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipConfig config) {
			tooltip.add(Text.of("This tooltip should mention the knockback resistance."));
			super.appendTooltip(stack, context, tooltip, config);
		}
	};

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(
				Registries.ITEM,
				Identifier.of(mod.metadata().id(), "knockback_resistance_chestplate"),
				KNOCKBACK_RESISTANCE_CHESTPLATE
		);
	}
}
