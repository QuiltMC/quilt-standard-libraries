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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;

/**
 * An extension of the default {@link Enchantment} class.
 * <p>
 * Allows for custom weighting in randomized enchanting contexts, as well as custom logic for application in the anvil.
 */
public abstract class QuiltEnchantment extends Enchantment {
	public QuiltEnchantment(Rarity rarity, @Nullable EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(rarity, type, slotTypes);
	}

	/**
	 * Return an integer value that represents the weight of this enchantment given the current context.
	 * <p>
	 * If 0, then this enchantment won't be added.
	 * @param context the context of the enchanting
	 * @return the context-aware weight for the enchantment
	 */
	public @Range(from = 0, to = Integer.MAX_VALUE) int weightFromContext(EnchantingContext context) {
		return this.getRarity().getWeight();
	}

	/**
	 * Determines if the given enchantment can be applied under the current context.
	 * <p>
	 * Note: {@link QuiltEnchantableItem#canEnchant(ItemStack, Enchantment)} takes priority.
	 * @see QuiltEnchantableItem#canEnchant(ItemStack, Enchantment)
	 * @param context the context of the enchanting
	 * @return {@code true} if this enchantment can be applied, or {@code false} otherwise
	 */
	public boolean isAcceptableContext(EnchantingContext context) {
		if (!context.ignoresPower() && this.isAcceptablePower(context.getLevel(), context.getPower())) {
			return false;
		}

		return this.isAcceptableItem(context.getStack());
	}

	/**
	 * Determines if the provided power is within the valid range for this enchantment at the provided level.
	 * @param level the level of this enchantment being queried
	 * @param power the power of the current enchanting context
	 * @return {@code true} if the power is within the valid range, or {@code false} otherwise
	 */
	public boolean isAcceptablePower(int level, int power) {
		return power >= this.getMinPower(level) && power <= this.getMaxPower(level);
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
