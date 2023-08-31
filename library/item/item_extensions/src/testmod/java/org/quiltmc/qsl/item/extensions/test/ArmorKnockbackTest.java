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

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorItem.ArmorSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ArmorKnockbackTest implements ModInitializer {
	private static final ArmorMaterial KNOCKBACK_RESISTANCE_ARMOR = new ArmorMaterial() {
		@Override
		public int getDurability(ArmorSlot slot) {
			return 10;
		}

		@Override
		public int getProtection(ArmorSlot slot) {
			return 0;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}

		@Override
		public String getName() {
			return "knockback_resistance";
		}

		@Override
		public float getToughness() {
			return 0;
		}

		@Override
		public float getKnockbackResistance() {
			return 200;
		}
	};

	private static final ArmorItem KNOCKBACK_RESISTANCE_CHESTPLATE = new ArmorItem(
			KNOCKBACK_RESISTANCE_ARMOR,
			ArmorSlot.CHESTPLATE,
			new Item.Settings().rarity(Rarity.RARE)
	) {
		@Override
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
			tooltip.add(Text.of("This tooltip should mention the knockback resistance."));
			super.appendTooltip(stack, world, tooltip, context);
		}
	};

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(
				Registries.ITEM,
				new Identifier(mod.metadata().id(), "knockback_resistance_chestplate"),
				KNOCKBACK_RESISTANCE_CHESTPLATE
		);
	}
}
