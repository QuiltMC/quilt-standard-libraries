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
 * @apiNote Used when u want to specify custom fluid flow direction, otherwise use QuiltFluid
 */
public abstract class QuiltDirectionalFluid extends QuiltFluid implements QuiltFlowableFluidExtensions{

	public Direction getFlowingDirection() {return Direction.DOWN;}

	@Override
	public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
		double d = 0.0;
		double e = 0.0;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
			mutable.set(pos, direction);
			FluidState fluidState = world.getFluidState(mutable);
			if (!this.isEmptyOrThis(fluidState)) continue;
			float f = fluidState.getHeight();
			float g = 0.0f;
			if (f == 0.0f) {
				FluidState fluidState2;
				if (!world.getBlockState(mutable).getMaterial().blocksMovement() && this.isEmptyOrThis(fluidState2 = world.getFluidState(mutable.move(getFlowingDirection()))) && (f = fluidState2.getHeight()) > 0.0f) {
					g = state.getHeight() - (f - 0.8888889f);
				}
			} else if (f > 0.0f) {
				g = state.getHeight() - f;
			}
			if (g == 0.0f) continue;
			d += (float)direction.getOffsetX() * g;
			e += (float)direction.getOffsetZ() * g;
		}
		Vec3d vec3d = new Vec3d(d, 0.0, e);
		if (state.get(FALLING).booleanValue()) {
			for (Direction direction2 : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
				mutable.set(pos, direction2);
				if (!this.m_innettlj(world, mutable, direction2) && !this.m_innettlj(world, mutable.move(getFlowingDirection().getOpposite()), direction2)) continue;
				vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
				break;
			}
		}
		return vec3d.normalize();
	}

	@Override
	protected boolean m_innettlj(BlockView world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos);
		FluidState fluidState = world.getFluidState(pos);
		if (fluidState.getFluid().matchesType(this)) {
			return false;
		}
		if (direction == getFlowingDirection().getOpposite()) {
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
		BlockPos blockPos = fluidPos.offset(getFlowingDirection());
		BlockState blockState2 = world.getBlockState(blockPos);
		FluidState fluidState = this.getUpdatedState(world, blockPos, blockState2);
		if (this.canFlow(world, fluidPos, blockState, getFlowingDirection(), blockPos, blockState2, world.getFluidState(blockPos), fluidState.getFluid())) {
			this.flow(world, blockPos, blockState2, getFlowingDirection(), fluidState);
			if (this.getFlowDist(world, fluidPos) >= 3) {
				this.m_hyyevjfk(world, fluidPos, state, blockState);
			}
		} else if (state.isSource() || !this.canFlowAndFill(world, fluidState.getFluid(), fluidPos, blockState, blockPos, blockState2)) {
			this.m_hyyevjfk(world, fluidPos, state, blockState);
		}
	}

	@Override
	public FluidState getUpdatedState(WorldView world, BlockPos pos, BlockState state) {
		BlockPos blockPos2;
		BlockState blockState3;
		FluidState fluidState3;
		int i = 0;
		int j = 0;
		for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
			BlockPos blockPos = pos.offset(direction);
			BlockState blockState = world.getBlockState(blockPos);
			FluidState fluidState = blockState.getFluidState();
			if (!fluidState.getFluid().matchesType(this) || !this.receivesFlow(direction, world, pos, state, blockPos, blockState)) continue;
			if (fluidState.isSource()) {
				++j;
			}
			i = Math.max(i, fluidState.getLevel());
		}
		if (this.isInfinite() && j >= 2) {
			BlockState blockState2 = world.getBlockState(pos.offset(getFlowingDirection()));
			FluidState fluidState2 = blockState2.getFluidState();
			if (blockState2.getMaterial().isSolid() || this.isMatchingAndStill(fluidState2)) {
				return this.getStill(false);
			}
		}
		if (!(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = pos.offset(getFlowingDirection().getOpposite()))).getFluidState()).isEmpty() && fluidState3.getFluid().matchesType(this) && this.receivesFlow(getFlowingDirection().getOpposite(), world, pos, state, blockPos2, blockState3)) {
			return this.getFlowing(8, true);
		}
		int k = i - this.getLevelDecreasePerBlock(world);
		if (k <= 0) {
			return Fluids.EMPTY.getDefaultState();
		}
		return this.getFlowing(k, false);
	}

	@Override
	protected int m_elhudbgf(WorldView world, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
		int j = 1000;
		for (Direction direction2 : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
			int k;
			if (direction2 == direction) continue;
			BlockPos blockPos3 = blockPos.offset(direction2);
			short s2 = this.getMask(blockPos2, blockPos3);
			Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
				BlockState blockState1 = world.getBlockState(blockPos3);
				return Pair.of(blockState1, blockState1.getFluidState());
			});
			BlockState blockState2 = (BlockState)pair.getFirst();
			FluidState fluidState = (FluidState)pair.getSecond();
			if (!this.canFlowThrough(world, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) continue;
			boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
				BlockPos blockPos4 = blockPos3.offset(getFlowingDirection());
				BlockState blockState4 = world.getBlockState(blockPos4);
				return this.canFlowAndFill(world, this.getFlowing(), blockPos3, blockState2, blockPos4, blockState4);
			});
			if (bl) {
				return i;
			}
			if (i >= this.getFlowSpeed(world) || (k = this.m_elhudbgf(world, blockPos3, i + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) >= j) continue;
			j = k;
		}
		return j;
	}

	public final boolean canFlowAndFill(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
		if (!this.receivesFlow(getFlowingDirection(), world, pos, state, fromPos, fromState)) {
			return false;
		}
		if (fromState.getFluidState().getFluid().matchesType(this)) {
			return true;
		}
		return this.canFill(world, fromPos, fromState, fluid);
	}

	public int getFlowDist(WorldView world, BlockPos pos) {
		int i = 0;
		for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
			BlockPos blockPos = pos.offset(direction);
			FluidState fluidState = world.getFluidState(blockPos);
			if (!this.isMatchingAndStill(fluidState)) continue;
			++i;
		}
		return i;
	}

	@Override
	protected Map<Direction, FluidState> getSpread(WorldView world, BlockPos pos, BlockState state) {
		int i = 1000;
		EnumMap<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
		Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
		Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
		for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
			BlockPos blockPos = pos.offset(direction);
			short s2 = this.getMask(pos, blockPos);
			Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
				BlockState blockState = world.getBlockState(blockPos);
				return Pair.of(blockState, blockState.getFluidState());
			});
			BlockState blockState = (BlockState)pair.getFirst();
			FluidState fluidState = (FluidState)pair.getSecond();
			FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
			if (!this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) continue;
			BlockPos blockPos2 = blockPos.offset(getFlowingDirection());
			boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
				BlockState blockState2 = world.getBlockState(blockPos2);
				return this.canFlowAndFill(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
			});
			int j = bl ? 0 : this.m_elhudbgf(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
			if (j < i) {
				map.clear();
			}
			if (j > i) continue;
			map.put(direction, fluidState2);
			i = j;
		}
		return map;
	}

	public static boolean isFluidInDirectionEqual(FluidState state, BlockView world, BlockPos pos) {
		if(state.getFluid() instanceof QuiltDirectionalFluid directionalFluid)
			return state.getFluid().matchesType(world.getFluidState(pos.offset(directionalFluid.getFlowingDirection())).getFluid());
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
		int i = blockPos2.getX() - blockPos.getX();
		int j = blockPos2.getZ() - blockPos.getZ();
		return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
	}
}
