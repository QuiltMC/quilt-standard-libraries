package org.quiltmc.qsl.inworldrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an <em>in-world recipe</em> - a recipe that can be performed on a specific set of block types
 * directly in the world.
 */
public interface InWorldRecipe {
	/**
	 * {@return the specific set of block types required for this recipe}
	 */
	@NotNull Set<Block> targetBlocks();

	/**
	 * Tries to perform the recipe.
	 * @param context item usage context
	 * @return {@code true} if recipe was successfully performed, {@code false} otherwise
	 */
	boolean tryPerform(@NotNull ItemUsageContext context);

	/**
	 * Invoked on data pack reload. Use this to update your {@linkplain #targetBlocks() target block set}.
	 */
	default void onReload() { }
}
