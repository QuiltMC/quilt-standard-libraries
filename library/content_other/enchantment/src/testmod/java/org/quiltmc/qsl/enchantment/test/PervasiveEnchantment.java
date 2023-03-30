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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.enchantment.api.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;

public class PervasiveEnchantment extends QuiltEnchantment {
	public PervasiveEnchantment() {
		super(Rarity.COMMON, null, EquipmentSlot.values());
	}

	@Override
	public int weightFromContext(EnchantingContext context) {
		return 20;
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isAcceptableContext(EnchantingContext context) {
		return true;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public void onTargetDamaged(LivingEntity user, Entity target, int level) {
		super.onTargetDamaged(user, target, level);

		this.infect(target, level);
	}

	@Override
	public void onUserDamaged(LivingEntity user, Entity attacker, int level) {
		super.onUserDamaged(user, attacker, level);

		this.infect(attacker, level);
	}

	private void infect(Entity target, int level) {
		target.getItemsEquipped().forEach(stack -> {
			if (stack.getItem().isEnchantable(stack)) {
				if (stack.hasEnchantments()) {
					var enchantments = EnchantmentHelper.get(stack);
					enchantments.put(this, level);
					EnchantmentHelper.set(enchantments, stack);
				} else {
					stack.addEnchantment(this, level);
				}
			}
		});
	}
}
