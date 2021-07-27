package org.quiltmc.qsl.toolinteractionrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a <em>tool interaction recipe</em> - a recipe that can be performed on a specific set of block types
 * directly in the world via using a tool on said block types.
 */
public interface ToolInteractionRecipe {
	/**
	 * Returns the specific set of block types required for this recipe.<p>
	 * Can be empty, which will be interpreted as this recipe being applicable to <em>every</em> block type.
	 *
	 * @return the specific set of block types required for this recipe
	 */
	@NotNull Set<Block> targetBlocks();

	/**
	 * Tries to perform the recipe.<p>
	 * If the recipe was successfully performed, call this method (via {@code super.tryPerform(context)} to damage the
	 * item by one.
	 * @param context item usage context
	 * @return {@code true} if recipe was successfully performed, {@code false} otherwise
	 */
	default boolean tryPerform(@NotNull ItemUsageContext context) {
		if (!context.getWorld().isClient) {
			PlayerEntity player = context.getPlayer();
			if (player != null)
				context.getStack().damage(1, player, (p) -> p.sendToolBreakStatus(context.getHand()));
		}
		return true;
	}
}
