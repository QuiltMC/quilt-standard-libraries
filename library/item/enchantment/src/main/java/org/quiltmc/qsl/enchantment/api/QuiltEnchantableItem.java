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

import org.jetbrains.annotations.Contract;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An interface for extending an {@link Item} with additional control over enchanting.
 */
public interface QuiltEnchantableItem {
	/**
	 * Determines whether the provided enchantment can be applied to this item.
	 * <p>
	 * This takes highest priority for applying enchantments.
	 * @param stack the stack containing this item
	 * @param enchantment the enchantment to apply to this item
	 * @return {@code true} if the enchantment can be applied, or {@code false} otherwise
	 */
	@Contract(pure = true)
	default boolean canEnchant(ItemStack stack, Enchantment enchantment) {
		return enchantment.isAcceptableItem(stack);
	}
}
