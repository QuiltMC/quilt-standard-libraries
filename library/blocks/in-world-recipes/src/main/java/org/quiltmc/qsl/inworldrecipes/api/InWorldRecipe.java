package org.quiltmc.qsl.inworldrecipes.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an <em>in-world recipe</em> - An recipe that can be performed on a specific block type in the world.
 */
public interface InWorldRecipe {
	/**
	 * {@return the specific block type required for this recipe}
	 */
	@NotNull Block targetBlock();

	/**
	 * Tries to perform the recipe.
	 * @param context item usage context
	 * @return {@code true} if recipe was successfully performed, {@code false} otherwise
	 */
	boolean tryPerform(@NotNull ItemUsageContext context);
}
