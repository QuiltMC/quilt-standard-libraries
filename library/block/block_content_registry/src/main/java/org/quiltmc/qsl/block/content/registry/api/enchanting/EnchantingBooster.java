package org.quiltmc.qsl.block.content.registry.api.enchanting;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The interface in charge of calculating the enchanting boost value.
 */
public interface EnchantingBooster {
	/**
	 * Gets the current boost level for the given parameter.
	 *
	 * @param world The current world
	 * @param state The block state
	 * @param pos   The position of the block
	 * @return The boost level
	 */
	float getEnchantingBoost(World world, BlockState state, BlockPos pos);

	/**
	 * @return The type for this booster.
	 */
	EnchantingBoosterType getType();
}
