/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.fluid.impl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class QuiltFluid extends FlowableFluid {
	/**
	 * @return whether the given fluid an instance of this fluid
	 */
	@Override
	public boolean matchesType(Fluid fluid) {
		return fluid == getStill() || fluid == getFlowing();
	}

	/**
	 * @return whether the fluid infinite like water
	 */
	@Override
	protected boolean isInfinite() {
		return false;
	}

	/**
	 * Perform actions when fluid flows into a replaceable block. Water drops
	 * the block's loot table. Lava plays the "block.lava.extinguish" sound.
	 */
	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
		final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, blockEntity);
	}

	/**
	 * Lava returns true if its FluidState is above a certain height and the
	 * Fluid is Water.
	 *
	 * @return whether the given Fluid can flow into this FluidState
	 */
	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
		return false;
	}

	/**
	 * Possibly related to the distance checks for flowing into nearby holes?
	 * Water returns 4. Lava returns 2 in the Overworld and 4 in the Nether.
	 */
	@Override
	protected int getFlowSpeed(WorldView worldView) {
		return 4;
	}

	/**
	 * Water returns 1. Lava returns 2 in the Overworld and 1 in the Nether.
	 */
	@Override
	protected int getLevelDecreasePerBlock(WorldView worldView) {
		return 1;
	}

	/**
	 * Water returns 5. Lava returns 30 in the Overworld and 10 in the Nether.
	 */
	@Override
	public int getTickRate(WorldView worldView) {
		return 5;
	}

	/**
	 * Water and Lava both return 100.0F.
	 */
	@Override
	protected float getBlastResistance() {
		return 100.0F;
	}

	public boolean canFishIn() {
		return true;
	}
}
