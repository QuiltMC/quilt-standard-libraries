package org.quiltmc.qsl.points_of_interest.test;

import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.points_of_interest.api.PointOfInterestHelper;

public class MainInitializer implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		PointOfInterestHelper.addBlocks(PointOfInterestType.ARMORER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL);
		PointOfInterestHelper.addBlockStates(PointOfInterestType.CARTOGRAPHER, Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true));

		PointOfInterestHelper.register(new Identifier("quilt_points_of_interest_testmod", "test_poi"), 1, 1, Blocks.DIAMOND_BLOCK);
	}
}
