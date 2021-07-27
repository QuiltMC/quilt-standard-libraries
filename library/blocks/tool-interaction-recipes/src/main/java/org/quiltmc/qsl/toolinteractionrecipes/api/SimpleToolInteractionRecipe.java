package org.quiltmc.qsl.toolinteractionrecipes.api;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a tool interaction recipe with a constant, unchanging set of target block types.
 */
public abstract class SimpleToolInteractionRecipe implements ToolInteractionRecipe {
	private final @NotNull Set<Block> targetBlocks;

	public SimpleToolInteractionRecipe(@NotNull Set<Block> targetBlocks) {
		this.targetBlocks = targetBlocks;
	}

	public SimpleToolInteractionRecipe(@NotNull Block targetBlock) {
		this(Collections.singleton(targetBlock));
	}

	@Override
	public @NotNull Set<Block> targetBlocks() {
		return targetBlocks;
	}
}
