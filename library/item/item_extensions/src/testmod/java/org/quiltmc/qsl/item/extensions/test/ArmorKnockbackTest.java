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

import static org.quiltmc.qsl.item.extensions.test.ItemExtensionTestUtil.createId;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.item.TooltipConfig;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_idvlscju;
import net.minecraft.util.EquipmentAssets;
import net.minecraft.util.Rarity;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ArmorKnockbackTest implements ModInitializer {
	private static final ArmorMaterial KNOCKBACK_RESISTANCE_ARMOR = new ArmorMaterial(
			1000,
			Map.of(
				ArmorType.BOOTS, 0,
				ArmorType.LEGGINGS, 0,
				ArmorType.CHESTPLATE, 0,
				ArmorType.HELMET, 0,
				ArmorType.BODY, 0
			),
			// this must be greater than 0
			1,
			Registries.SOUND_EVENT.wrapAsHolder(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME),
			0.0f,
			200.0F,
			ItemTags.WOOL,
			RegistryKey.of(EquipmentAssets.REGISTRY, createId("knockback_armor"))
	);

	private static final RegistryKey<Item> KNOCKBACK_RESISTANCE_CHESTPLATE_KEY =
			ItemExtensionTestUtil.createItemKey("knockback_resistance_chestplate");

	private static final Item KNOCKBACK_RESISTANCE_CHESTPLATE = new Item(
			new Item.Settings().rarity(Rarity.RARE)
				.key(KNOCKBACK_RESISTANCE_CHESTPLATE_KEY)
				.method_66332(KNOCKBACK_RESISTANCE_ARMOR, ArmorType.CHESTPLATE)
	) {
		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, C_idvlscju c_idvlscju, Consumer<Text> consumer, TooltipConfig config) {
			super.appendTooltip(stack, context, c_idvlscju, consumer, config);
		}
	};

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(
				Registries.ITEM,
				KNOCKBACK_RESISTANCE_CHESTPLATE_KEY,
				KNOCKBACK_RESISTANCE_CHESTPLATE
		);
	}
}
