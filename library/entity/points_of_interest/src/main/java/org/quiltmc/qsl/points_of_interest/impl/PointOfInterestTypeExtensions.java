package org.quiltmc.qsl.points_of_interest.impl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.Collection;

public interface PointOfInterestTypeExtensions {
	void addBlocks(Collection<Block> blocks);

	void addBlockStates(Collection<BlockState> states);
}
