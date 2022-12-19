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
	 * @param world the current world
	 * @param state the block state
	 * @param pos   the position of the block
	 * @return the boost level
	 */
	float getEnchantingBoost(World world, BlockState state, BlockPos pos);

	/**
	 * @return the type for this booster.
	 */
	EnchantingBoosterType getType();
}
