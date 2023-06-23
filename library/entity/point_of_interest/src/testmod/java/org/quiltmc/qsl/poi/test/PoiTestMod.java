/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.poi.test;

import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestTypes;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.poi.api.PointOfInterestHelper;

public class PoiTestMod implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		PointOfInterestHelper.addBlocks(PointOfInterestTypes.ARMORER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL);
		PointOfInterestHelper.addBlockStates(PointOfInterestTypes.CARTOGRAPHER, Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true));

		PointOfInterestHelper.setBlocks(PointOfInterestTypes.LEATHERWORKER, Blocks.OAK_TRAPDOOR);
		PointOfInterestHelper.addBlocks(PointOfInterestTypes.FISHERMAN, Blocks.CAULDRON);

		PointOfInterestHelper.register(new Identifier("quilt_point_of_interest_testmod", "test_poi"), 1, 1, Blocks.DIAMOND_BLOCK);
	}
}
