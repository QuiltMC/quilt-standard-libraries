package org.quiltmc.qsl.inworldrecipes.api;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Represents an in-world recipe with a constant, unchanging set of target block types.
 */
public abstract class SimpleInWorldRecipe implements InWorldRecipe {
	private final @NotNull Set<Block> targetBlocks;

	public SimpleInWorldRecipe(@NotNull Set<Block> targetBlocks) {
		this.targetBlocks = targetBlocks;
	}

	public SimpleInWorldRecipe(@NotNull Block targetBlock) {
		this(Collections.singleton(targetBlock));
	}

	@Override
	public @NotNull Set<Block> targetBlocks() {
		return targetBlocks;
	}
}
