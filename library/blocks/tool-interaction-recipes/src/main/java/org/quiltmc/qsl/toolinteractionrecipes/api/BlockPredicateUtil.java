package org.quiltmc.qsl.toolinteractionrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.mixin.BlockPredicateAccessor;

import java.util.HashSet;
import java.util.Set;

public final class BlockPredicateUtil {
	private BlockPredicateUtil() { throw new AssertionError(); }

	public @NotNull Set<Block> getBlocks(@NotNull BlockPredicate predicate) {
		Tag<Block> tag = ((BlockPredicateAccessor) predicate).getTag();
		if (tag != null)
			return new HashSet<>(tag.values()); // /shrug
		return ((BlockPredicateAccessor) predicate).getBlocks();
	}
}
