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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.random.RandomGenerator;

import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;

/**
 * Allows modded systems that enchant items to apply an enchanting context.
 */
public final class QuiltEnchantmentHelper {
	private static final ThreadLocal<EnchantingContext> CONTEXT = new ThreadLocal<>();

	/**
	 * Set the enchanting context for enchantments to use.
	 * <p>
	 * Note: Almost all the base values, bar the {@link net.minecraft.world.World world}, are provided to the context
	 * when using {@link EnchantmentHelper#enchant(RandomGenerator, ItemStack, int, boolean)} or
	 * {@link EnchantmentHelper#generateEnchantments(RandomGenerator, ItemStack, int, boolean)}.
	 * @param context the enchanting context
	 */
	public static void setContext(EnchantingContext context) {
		CONTEXT.set(context);
	}

	/**
	 * Gets the current enchanting context.
	 * @return the enchanting context
	 */
	public static @Nullable EnchantingContext getContext() {
		return CONTEXT.get();
	}

	/**
	 * Clears the current enchanting context.
	 * <p>
	 * This should be used to ensure that no information bleeds into other contexts.
	 */
	public static void clearContext() {
		CONTEXT.remove();
	}
}
