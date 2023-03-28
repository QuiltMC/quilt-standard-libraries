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

import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.api.AnvilContext;
import org.quiltmc.qsl.enchantment.api.EnchantmentContext;

public class ReapingEnchantment extends QuiltEnchantment {
	public ReapingEnchantment() {
		super(Rarity.COMMON, null, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
	}

	@Override
	public int weightFromEnchantmentContext(EnchantmentContext context) {
		return super.weightFromEnchantmentContext(context) > 0 && context.stack().getItem() instanceof HoeItem ? 10 : 0;
	}

	@Override
	public boolean isAcceptableAnvilContext(AnvilContext context) {
		return context.pos().getX() % 2 == 0;
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
