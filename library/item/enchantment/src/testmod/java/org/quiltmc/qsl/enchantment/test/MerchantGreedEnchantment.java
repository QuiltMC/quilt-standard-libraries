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

import org.jetbrains.annotations.Range;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.context.EntityEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;

public class MerchantGreedEnchantment extends QuiltEnchantment {
	public MerchantGreedEnchantment() {
		super(Rarity.COMMON, null, EquipmentSlot.values());
	}

	@Override
	public @Range(from = 0, to = Integer.MAX_VALUE) int weightFromContext(EnchantingContext context) {
		return 100;
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return stack.getItem().isEnchantable(stack);
	}

	@Override
	public boolean isAcceptableContext(EnchantingContext context) {
		return context instanceof EntityEnchantingContext<?> entityEnchantingContext && entityEnchantingContext.getEntity() instanceof MerchantEntity;
	}

	@Override
	public boolean isVisible(ItemGroup.Visibility visibility) {
		return false;
	}
}
