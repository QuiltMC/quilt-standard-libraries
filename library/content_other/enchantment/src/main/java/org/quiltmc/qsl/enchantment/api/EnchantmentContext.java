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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A class that contains contextual information about the enchantment process.
 *
 * @param level     An integer representing the current enchantment level being queried (ie. I, II, III, etc.
 * @param power     The current power of the enchantment table.
 * @param bookcases The amount of bookcases around the enchantment table.
 * @param stack     The item stack for the enchantment to be applied to.
 * @param world     The world in which the item is being enchanted.
 * @param player    The player currently trying to enchant the item.
 * @param pos       The block position of the enchantment table.
 */
public record EnchantmentContext(int level, int power, int bookcases, ItemStack stack, World world, PlayerEntity player, BlockPos pos) {
	public EnchantmentContext withLevel(int level) {
		return new EnchantmentContext(level, this.power, this.bookcases, this.stack, this.world, this.player, this.pos);
	}

	public EnchantmentContext withPower(int power) {
		return new EnchantmentContext(this.level, power, this.bookcases, this.stack, this.world, this.player, this.pos);
	}
}
