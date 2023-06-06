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

package org.quiltmc.qsl.fluid.flow.api;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.quiltmc.qsl.base.api.event.Event;

public final class FluidFlowEvents {
	private FluidFlowEvents() {}

	// Flowing Block to Interacting Block to Event
	private static final Map<Block, Map<Block, Event<FluidFlowInteractionCallback>>> EVENT_MAP = new Object2ObjectOpenHashMap<>();

	/**
	 * Registers a new event on a fluid flow.
	 *
	 * @param flowingBlock     the fluid block that flowed
	 * @param interactionBlock the block in one of the {@code interactionDirections}
	 * @param interactionEvent the event to run when the conditions are met
	 */
	public static void register(Block flowingBlock, Block interactionBlock, FluidFlowInteractionCallback interactionEvent) {
		var flowBlockEvents = EVENT_MAP.computeIfAbsent(flowingBlock, flowing -> new Object2ObjectOpenHashMap<>());
		flowBlockEvents.computeIfAbsent(interactionBlock, (block) -> Event.create(FluidFlowInteractionCallback.class, fluidFlowInteractionEvents -> (flowingBlockState, interactingBlockState, interactionDirection, flowPos, world) -> {
			for (FluidFlowInteractionCallback event : fluidFlowInteractionEvents) {
				if (!event.onFlow(flowingBlockState, interactingBlockState, interactionDirection, flowPos, world)) {
					return false;
				}
			}

			return true;
		})).register(interactionEvent);
	}

	/**
	 * Gets the event from the following blocks and direction.
	 *
	 * @param flowingBlock     the fluid block that flowed
	 * @param interactionBlock the block it interacts with
	 * @return an event if the conditions are met, otherwise {@code null}
	 */
	public static @Nullable Event<FluidFlowInteractionCallback> getEvent(Block flowingBlock, Block interactionBlock) {
		var flowBlockEvents = EVENT_MAP.get(flowingBlock);
		if (flowBlockEvents != null) {
			return flowBlockEvents.get(interactionBlock);
		}

		return null;
	}

	public interface FluidFlowInteractionCallback {
		/**
		 * An event run when a fluid flows next to a block.
		 *
		 * @param flowingBlockState     The block state of the fluid block.
		 * @param interactingBlockState The block state of the interacting block.
		 * @param interactionDirection  The direction from the flowingBlockState to the interactingBlockState.
		 * @param flowPos               The position in the world that the fluid flowed into.
		 * @param world                 The world the event took place in.
		 * @return {@code false} if the event successfully ran, and {@code true} if it was unsuccessful or did not run.
		 */
		boolean onFlow(BlockState flowingBlockState, BlockState interactingBlockState, Direction interactionDirection, BlockPos flowPos, World world);
	}
}
