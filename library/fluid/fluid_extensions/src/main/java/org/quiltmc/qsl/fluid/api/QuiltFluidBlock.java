package org.quiltmc.qsl.fluid.api;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;

public class QuiltFluidBlock extends FluidBlock {

	/**
	 * Utility class, to not deal with anonymous classes.
	 */
	public QuiltFluidBlock(FlowableFluid flowableFluid, Settings settings) {
		super(flowableFluid, settings);
	}
}
