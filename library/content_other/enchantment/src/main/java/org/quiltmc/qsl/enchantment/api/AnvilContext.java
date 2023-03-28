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
 * A record that contains contextual information about the enchantment application process in an anvil.
 *
 * @param level  An integer representing the current enchantment level of the second item stack.
 * @param stack  The item stack for the enchantment to be applied to.
 * @param world  The world in which the item is being enchanted.
 * @param player The player currently trying to enchant the item.
 * @param pos    The block position of the anvil.
 */
public record AnvilContext(int level, ItemStack stack, World world, PlayerEntity player, BlockPos pos) {
	public AnvilContext withLevel(int level) {
		return new AnvilContext(level, this.stack, this.world, this.player, this.pos);
	}
}
