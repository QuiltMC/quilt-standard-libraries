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

package org.quiltmc.qsl.fluid.api;


import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.EnumMap;
import java.util.Map;

/**
 * @apiNote Used when specifying a custom fluid flow direction is required, otherwise use QuiltFluid.
 * @implNote A lot of this code is refactored from the FlowableFluid Class, a long with the magic values. A lot of these values should not be touched, as there is no proper reason to yet.
 */
public abstract class QuiltDirectionalFluid extends QuiltFluid implements QuiltFlowableFluidExtensions{

	/**
	 * Overwrite this when wanting to change the flowing Direction of the Fluid
	 * @return Direction the fluid will flow to.
	 */
	public Direction getFlowDirection() {return Direction.DOWN;}

	/**
	 *
	 * @param world - The world the fluid resides in
	 * @param pos - The position of the fluid
	 * @param state - The state of the fluid
	 * @return - The velocity of the fluid
	 */
	@Override
	public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
		double offsetX = 0.0;
		double offsetZ = 0.0;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (Direction direction : Direction.Type.HORIZONTAL) {
			mutable.set(pos, direction);
			FluidState fluidState = world.getFluidState(mutable);
			if (!this.isEmptyOrThis(fluidState)) continue;
			float fluidHeight = fluidState.getHeight();
			float modifiedFluidHeight = 0.0f;
			if (fluidHeight == 0.0f) {
				FluidState fluidState2;
				if (!world.getBlockState(mutable).getMaterial().blocksMovement() && this.isEmptyOrThis(fluidState2 = world.getFluidState(mutable.move(getFlowDirection()))) && (fluidHeight = fluidState2.getHeight()) > 0.0f) {
					modifiedFluidHeight = state.getHeight() - (fluidHeight - 0.8888889f);
				}
			} else if (fluidHeight > 0.0f) {
				modifiedFluidHeight = state.getHeight() - fluidHeight;
			}
			if (modifiedFluidHeight == 0.0f) continue;
			offsetX += (float)direction.getOffsetX() * modifiedFluidHeight;
			offsetZ += (float)direction.getOffsetZ() * modifiedFluidHeight;
		}
		Vec3d vec3d = new Vec3d(offsetX, 0.0, offsetZ);
		if (state.get(FALLING).booleanValue()) {
			for (Direction direction2 : Direction.Type.HORIZONTAL) {
				mutable.set(pos, direction2);
				if (!this.m_innettlj(world, mutable, direction2) && !this.m_innettlj(world, mutable.move(getFlowDirection().getOpposite()), direction2)) continue;
				vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
				break;
			}
		}
		return vec3d.normalize();
	}

	/**
	 *
	 * @param world - The world the fluid resides in
	 * @param pos - The position of the fluid
	 * @param direction - The direction the fluid is flowing
	 * @return - Whether the fluid can flow there or not.
	 */
	@Override
	protected boolean m_innettlj(BlockView world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos);
		FluidState fluidState = world.getFluidState(pos);
		if (fluidState.getFluid().matchesType(this)) {
			return false;
		}
		if (direction == getFlowDirection().getOpposite()) {
			return true;
		}
		if (blockState.getMaterial() == Material.ICE) {
			return false;
		}
		return blockState.isSideSolidFullSquare(world, pos, direction);
	}

	@Override
	protected void tryFlow(WorldAccess world, BlockPos fluidPos, FluidState state) {
		if (state.isEmpty()) {
			return;
		}
		BlockState blockState = world.getBlockState(fluidPos);
		BlockPos blockPos = fluidPos.offset(getFlowDirection());
		BlockState blockState2 = world.getBlockState(blockPos);
		FluidState fluidState = this.getUpdatedState(world, blockPos, blockState2);
		if (this.canFlow(world, fluidPos, blockState, getFlowDirection(), blockPos, blockState2, world.getFluidState(blockPos), fluidState.getFluid())) {
			this.flow(world, blockPos, blockState2, getFlowDirection(), fluidState);
			if (this.getFlowDist(world, fluidPos) >= 3) {
				this.m_hyyevjfk(world, fluidPos, state, blockState);
			}
		} else if (state.isSource() || !this.canFlowAndFill(world, fluidState.getFluid(), fluidPos, blockState, blockPos, blockState2)) {
			this.m_hyyevjfk(world, fluidPos, state, blockState);
		}
	}

	/**
	 *
	 * @param world - The world the fluid resides in
	 * @param pos  - The position of the fluid
	 * @param state - The state of the fluid
	 * @return - The updated state for the fluid
	 */
	@Override
	public FluidState getUpdatedState(WorldView world, BlockPos pos, BlockState state) {
		BlockPos blockPos2;
		BlockState blockState3;
		FluidState fluidState3;
		int maxFluidLevel = 0;
		int sourceBlocks = 0;
		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos blockPos = pos.offset(direction);
			BlockState blockState = world.getBlockState(blockPos);
			FluidState fluidState = blockState.getFluidState();
			if (!fluidState.getFluid().matchesType(this) || !this.receivesFlow(direction, world, pos, state, blockPos, blockState)) continue;
			if (fluidState.isSource()) {
				++sourceBlocks;
			}
			maxFluidLevel = Math.max(maxFluidLevel, fluidState.getLevel());
		}
		if (this.isInfinite() && sourceBlocks >= 2) {
			BlockState blockState2 = world.getBlockState(pos.offset(getFlowDirection()));
			FluidState fluidState2 = blockState2.getFluidState();
			if (blockState2.getMaterial().isSolid() || this.isMatchingAndStill(fluidState2)) {
				return this.getStill(false);
			}
		}
		if (!(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = pos.offset(getFlowDirection().getOpposite()))).getFluidState()).isEmpty() && fluidState3.getFluid().matchesType(this) && this.receivesFlow(getFlowDirection().getOpposite(), world, pos, state, blockPos2, blockState3)) {
			return this.getFlowing(8, true);
		}
		int temp = maxFluidLevel - this.getLevelDecreasePerBlock(world);
		if (temp <= 0) {
			return Fluids.EMPTY.getDefaultState();
		}
		return this.getFlowing(temp, false);
	}

	/**
	 * Shouldn't really be used, as it's quite hard to get right on your own
	 * @return - The flow distance
	 */
	@Override
	protected int m_elhudbgf(WorldView world, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
		int maxFlowDistance = 1000;
		for (Direction direction2 : Direction.Type.HORIZONTAL) {
			int temp;
			if (direction2 == direction) continue;
			BlockPos blockPos3 = blockPos.offset(direction2);
			short blockPosMask = this.getMask(blockPos2, blockPos3);
			Pair<BlockState, FluidState> pair = short2ObjectMap.computeIfAbsent(blockPosMask, s -> {
				BlockState blockState1 = world.getBlockState(blockPos3);
				return Pair.of(blockState1, blockState1.getFluidState());
			});
			BlockState blockState2 = pair.getFirst();
			FluidState fluidState = pair.getSecond();
			if (!this.canFlowThrough(world, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) continue;
			boolean bl = short2BooleanMap.computeIfAbsent(blockPosMask, s -> {
				BlockPos blockPos4 = blockPos3.offset(getFlowDirection());
				BlockState blockState4 = world.getBlockState(blockPos4);
				return this.canFlowAndFill(world, this.getFlowing(), blockPos3, blockState2, blockPos4, blockState4);
			});
			if (bl) {
				return i;
			}
			if (i >= this.getFlowSpeed(world) || (temp = this.m_elhudbgf(world, blockPos3, i + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) >= maxFlowDistance) continue;
			maxFlowDistance = temp;
		}
		return maxFlowDistance;
	}

	/**
	 * If the fluid can flow to a position and waterlog the block
	 * @return - Whether the fluid can flow and fill the block at a given position or not
	 */
	public final boolean canFlowAndFill(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
		if (!this.receivesFlow(getFlowDirection(), world, pos, state, fromPos, fromState)) {
			return false;
		}
		if (fromState.getFluidState().getFluid().matchesType(this)) {
			return true;
		}
		return this.canFill(world, fromPos, fromState, fluid);
	}

	/**
	 * @param world - The world the fluid resides in
	 * @param pos  - The position of the fluid
	 * @return - The flow distance
	 */
	public int getFlowDist(WorldView world, BlockPos pos) {
		int flowDistance = 0;
		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos blockPos = pos.offset(direction);
			FluidState fluidState = world.getFluidState(blockPos);
			if (!this.isMatchingAndStill(fluidState)) continue;
			++flowDistance;
		}
		return flowDistance;
	}

	@Override
	protected Map<Direction, FluidState> getSpread(WorldView world, BlockPos pos, BlockState state) {
		int maxFlowDistance = 1000;
		EnumMap<Direction, FluidState> fluidDirectionMap = Maps.newEnumMap(Direction.class);
		Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
		Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos blockPos = pos.offset(direction);
			short blockPosMask = this.getMask(pos, blockPos);
			Pair<BlockState, FluidState> blockStateFluidStatePair = short2ObjectMap.computeIfAbsent(blockPosMask, s -> {
				BlockState blockState = world.getBlockState(blockPos);
				return Pair.of(blockState, blockState.getFluidState());
			});
			BlockState blockState = blockStateFluidStatePair.getFirst();
			FluidState fluidState = blockStateFluidStatePair.getSecond();
			FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
			if (!this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) continue;
			BlockPos blockPos2 = blockPos.offset(getFlowDirection());
			boolean canFlowAndFill = short2BooleanMap.computeIfAbsent(blockPosMask, s -> {
				BlockState blockState2 = world.getBlockState(blockPos2);
				return this.canFlowAndFill(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
			});
			int flowDistance = canFlowAndFill ? 0 : this.m_elhudbgf(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
			if (flowDistance < maxFlowDistance) {
				fluidDirectionMap.clear();
			}
			if (flowDistance > maxFlowDistance) continue;
			fluidDirectionMap.put(direction, fluidState2);
			maxFlowDistance = flowDistance;
		}
		return fluidDirectionMap;
	}

	public static boolean isFluidInDirectionEqual(FluidState state, BlockView world, BlockPos pos) {
		if(state.getFluid() instanceof QuiltDirectionalFluid directionalFluid)
			return state.getFluid().matchesType(world.getFluidState(pos.offset(directionalFluid.getFlowDirection())).getFluid());
		return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
	}

	@Override
	public float getHeight(FluidState state, BlockView world, BlockPos pos) {
		if (isFluidInDirectionEqual(state, world, pos)) {
			return 1.0f;
		}
		return state.getHeight();
	}

	@Override
	public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
		if (state.getLevel() == 9 && isFluidInDirectionEqual(state, world, pos)) {
			return VoxelShapes.fullCube();
		}
		return this.shapeCache.computeIfAbsent(state, fluidState -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(world, pos), 1.0));
	}

	private static short getMask(BlockPos blockPos, BlockPos blockPos2) {
		int xOffset = blockPos2.getX() - blockPos.getX();
		int zOffset = blockPos2.getZ() - blockPos.getZ();
		return (short)((xOffset + 128 & 0xFF) << 8 | zOffset + 128 & 0xFF);
	}
}
