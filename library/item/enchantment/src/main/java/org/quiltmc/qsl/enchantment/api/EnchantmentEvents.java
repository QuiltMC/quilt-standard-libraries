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

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;

public final class EnchantmentEvents {
	/**
	 * An event that is called after random enchantments are generated (e.g. for an enchantment table), but before they are selected to be applied on an item.
	 * @see EnchantmentHelper#getPossibleEntries(int, ItemStack, boolean)
	 */
	public static final Event<ModifyPossibleEnchantments> MODIFY_POSSIBLE_ENCHANTMENTS = Event.create(ModifyPossibleEnchantments.class, callbacks -> (possibleEnchantments, context) -> {
		for (var callback : callbacks) {
			callback.modifyPossibleEntries(possibleEnchantments, context);
		}
	});

	/**
	 * An event that is called when applying an {@link Enchantment} to an {@link Item} in an anvil.
	 * @see AnvilScreenHandler#updateResult()
	 */
	public static final Event<AnvilApplication> ANVIL_APPLICATION = Event.create(AnvilApplication.class, callbacks -> (enchantment, context) -> {
		for (var callback : callbacks) {
			if (!callback.canApply(enchantment, context)) {
				return false;
			}
		}

		return true;
	});

	@FunctionalInterface
	public interface ModifyPossibleEnchantments {
		@Contract(mutates = "param1")
		void modifyPossibleEntries(List<EnchantmentLevelEntry> possibleEnchantments, @Nullable EnchantingContext context);
	}

	@FunctionalInterface
	public interface AnvilApplication {
		@Contract(pure = true)
		boolean canApply(Enchantment enchantment, @Nullable EnchantingContext context);
	}
}
