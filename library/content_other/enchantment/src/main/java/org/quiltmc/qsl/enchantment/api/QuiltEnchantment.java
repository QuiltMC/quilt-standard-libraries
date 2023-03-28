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

package org.quiltmc.qsl.enchantment.api;

import org.jetbrains.annotations.Range;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * An extension of the default {@link Enchantment} class.
 * <p>
 * Allows for custom weighting in the enchantment table, as well as custom logic for application in the anvil.
 */
public abstract class QuiltEnchantment extends Enchantment {
	public QuiltEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		if (this.type == null) {
			return false;
		}

		return super.isAcceptableItem(stack);
	}

	/**
	 * Return an integer value that represents the weight of this enchantment given the current {@link EnchantmentContext context}.
	 * <p>
	 * If 0, then this enchantment won't be added.
	 * @param context the context of the enchanting
	 * @return the context-aware weight for the enchantment
	 */
	public @Range(from = 0, to = Integer.MAX_VALUE) int weightFromEnchantmentContext(EnchantmentContext context) {
		if (context.power() >= this.getMinPower(context.level()) && context.power() <= this.getMaxPower(context.level())) {
			return this.getRarity().getWeight();
		}

		return 0; // Not added at all
	}

	/**
	 * Determines if the given enchantment can be applied in the anvil under the current context.
	 * @param context the context of the enchanting
	 * @return {@code true} if this enchantment can be applied, or {@code false} otherwise
	 */
	public boolean isAcceptableAnvilContext(AnvilContext context) {
		return this.isAcceptableItem(context.stack());
	}

	/**
	 * Determines if the given enchantment should be visible in the creative tab.
	 * @param visibility in what context the stack visibility is tested
	 * @return {@code true} if this enchantment should be visible, or {@code false} otherwise
	 */
	public boolean isVisible(ItemGroup.Visibility visibility) {
		return true;
	}
}
