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

package org.quiltmc.qsl.enchantment.api.context;

import org.jetbrains.annotations.Contract;

import net.minecraft.item.ItemStack;
import net.minecraft.util.random.RandomGenerator;

/**
 * A class that contains contextual information about the enchantment process.
 */
public class EnchantingContext {
	protected final int level;
	protected final int power;
	protected final ItemStack stack;
	protected final RandomGenerator random;
	protected final boolean treasureAllowed;

	public EnchantingContext(int level, int power, ItemStack stack, RandomGenerator random, boolean treasureAllowed) {
		this.level = level;
		this.power = power;
		this.stack = stack;
		this.random = random;
		this.treasureAllowed = treasureAllowed;
	}

	@Contract("_->new")
	public EnchantingContext withLevel(int level) {
		return new EnchantingContext(level, this.power, this.stack, this.random, this.treasureAllowed);
	}

	@Contract("_->new")
	public EnchantingContext withPower(int power) {
		return new EnchantingContext(this.level, power, this.stack, this.random, this.treasureAllowed);
	}

	@Contract("_,_,_->new")
	public EnchantingContext withCoreContext(ItemStack stack, RandomGenerator random, boolean treasureAllowed) {
		return new EnchantingContext(this.level, this.power, stack, random, treasureAllowed);
	}

	/**
	 * @return an integer representing the current enchantment level being queried (ie. I, II, III, etc
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * @return the current power of the enchanting context
	 */
	public int getPower() {
		return this.power;
	}

	/**
	 * @return the item stack for the enchantment to be applied to
	 */
	public ItemStack getStack() {
		return this.stack;
	}

	/**
	 * @return the random used for enchanting
	 */
	public RandomGenerator getRandom() {
		return this.random;
	}

	/**
	 * @return {@code true} if treasure enchantments are allowed, or {@code false} otherwise
	 */
	public boolean treasureAllowed() {
		return this.treasureAllowed;
	}

	/**
	 * @return {@code true} if this context ignores power (i.e. Applying enchantments in an anvil), or {@code false} otherwise
	 */
	public boolean ignoresPower() {
		return this.power <= 0;
	}
}
