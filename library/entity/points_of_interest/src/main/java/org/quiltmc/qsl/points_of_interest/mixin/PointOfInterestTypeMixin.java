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

package org.quiltmc.qsl.points_of_interest.mixin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.qsl.points_of_interest.impl.PointOfInterestTypeExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mixin(PointOfInterestType.class)
public class PointOfInterestTypeMixin implements PointOfInterestTypeExtensions {

	@Shadow
	@Final
	@Mutable
	private Set<BlockState> blockStates;

	@Shadow
	@Final
	private static Map<BlockState, PointOfInterestType> BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE;

	@Override
	public void addBlocks(Collection<Block> blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			Set<BlockState> validStates = Sets.difference(Set.copyOf(block.getStateManager().getStates()), this.blockStates);
			builder.addAll(validStates);
			for (BlockState state : validStates) {
				PointOfInterestType replaced = BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE.put(state, (PointOfInterestType) (Object) this);
				if (replaced != null) {
					throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
				}
			}
		}

		this.blockStates = builder.addAll(this.blockStates).build();
	}

	@Override
	public void addBlockStates(Collection<BlockState> states) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		Set<BlockState> validStates = Sets.difference(Set.copyOf(states), this.blockStates);
		builder.addAll(validStates);
		for (BlockState state : validStates) {
			PointOfInterestType replaced = BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE.put(state, (PointOfInterestType) (Object) this);
			if (replaced != null) {
				throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
			}
		}

		this.blockStates = builder.addAll(this.blockStates).build();
	}
}
