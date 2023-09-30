/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
