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

package org.quiltmc.qsl.fluid.flow;

import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.fluid.flow.api.FluidFlowEvents;

public class FluidFlowEventTests implements ModInitializer {
	@Override
	public void onInitialize(ModContainer container) {
		FluidFlowEvents.register(Blocks.WATER, Blocks.BLUE_ICE, (flowingBlockState, interactingBlockState, interactionDirection, flowPos, world) -> {
			if (interactionDirection == Direction.DOWN) {
				world.setBlockState(flowPos, Blocks.ICE.getDefaultState());
				world.playSound(null, flowPos, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 1, 1);
				return false;
			}

			return true;
		});
	}
}
