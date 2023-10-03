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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

/**
 * A class that contains contextual information about the enchantment process.
 * <p>
 * Contains further information about the player and block performing the enchanting.
 */
public class PlayerUsingBlockEnchantingContext extends EntityEnchantingContext<PlayerEntity> {
	protected final BlockPos pos;

	public PlayerUsingBlockEnchantingContext(int level, int power, ItemStack stack, World world, RandomGenerator random, boolean treasureAllowed, PlayerEntity player, BlockPos pos) {
		super(level, power, stack, world, random, treasureAllowed, player);
		this.pos = pos;
	}

	@Override
	@Contract("_->new")
	public PlayerUsingBlockEnchantingContext withLevel(int level) {
		return new PlayerUsingBlockEnchantingContext(level, this.power, this.stack, this.world, this.random, this.treasureAllowed, this.entity, this.pos);
	}

	@Override
	@Contract("_->new")
	public EntityEnchantingContext<PlayerEntity> withPower(int power) {
		return new PlayerUsingBlockEnchantingContext(this.level, power, this.stack, this.world, this.random, this.treasureAllowed, this.entity, this.pos);
	}

	@Override
	@Contract("_,_,_->new")
	public EnchantingContext withCoreContext(ItemStack stack, RandomGenerator random, boolean treasureAllowed) {
		return new PlayerUsingBlockEnchantingContext(this.level, this.power, stack, this.world, random, treasureAllowed, this.entity, this.pos);
	}

	/**
	 * @return the player performing the enchanting
	 */
	public PlayerEntity getPlayer() {
		return this.getEntity();
	}

	/**
	 * @return the position of the block being used
	 */
	public BlockPos getPos() {
		return this.pos;
	}
}
