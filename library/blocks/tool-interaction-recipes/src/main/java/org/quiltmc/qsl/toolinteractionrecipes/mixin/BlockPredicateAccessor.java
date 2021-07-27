package org.quiltmc.qsl.toolinteractionrecipes.mixin;

import net.minecraft.block.Block;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockPredicate.class)
public interface BlockPredicateAccessor {
	@Accessor("blocks")
	Set<Block> getBlocks();
	@Accessor("tag")
	Tag<Block> getTag();
}
