/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.enchantment.test;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.context.PlayerUsingBlockEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;

public class ReapingEnchantment extends QuiltEnchantment {
	public ReapingEnchantment() {
		super(Rarity.COMMON, null, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
	}

	@Override
	public int weightFromContext(EnchantingContext context) {
		return context.getStack().isOf(Items.IRON_HOE) ? 20 : 10;
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return stack.getItem() instanceof HoeItem;
	}

	@Override
	public boolean isAcceptableContext(EnchantingContext context) {
		if (context instanceof PlayerUsingBlockEnchantingContext playerUsingBlockContext) {
			return playerUsingBlockContext.getPos().getX() % 2 == 0;
		}

		return super.isAcceptableContext(context);
	}

	@Override
	public boolean isVisible(ItemGroup.Visibility visibility) {
		return visibility == ItemGroup.Visibility.PARENT_TAB_ONLY;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
