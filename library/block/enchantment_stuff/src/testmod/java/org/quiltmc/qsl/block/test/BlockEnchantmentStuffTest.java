/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.block.test;

import net.minecraft.block.Blocks;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.enchantmentstuff.api.EnchantmentPowerAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockEnchantmentStuffTest implements ModInitializer {
	public static final String MOD_ID = "quilt_block_enchantment_stuff_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger("BlockEnchantmentStuffTest");


	public static boolean testPassed = false;

	@Override
	public void onInitialize(ModContainer mod) {
		EnchantmentPowerAttachment.POWER_LEVEL.put(Blocks.IRON_BLOCK, 3f);
		EnchantmentPowerAttachment.POWER_LEVEL.put(Blocks.DIAMOND_BLOCK, 15f);
		EnchantmentPowerAttachment.POWER_LEVEL.put(Blocks.NETHERITE_BLOCK, 100f);
		EnchantmentPowerAttachment.POWER_LEVEL.put(Blocks.OAK_PLANKS, 0.25f);
	}
}
