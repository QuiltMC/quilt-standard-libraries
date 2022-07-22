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

package org.quiltmc.qsl.rendering.item.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

/**
 * A {@link CooldownOverlayRenderer} implementation that replicates vanilla behavior.
 */
@Environment(EnvType.CLIENT)
public class VanillaCooldownOverlayRenderer extends SolidColorCooldownOverlayRenderer {
	/**
	 * Gets the cooldown progress of the item.
	 *
	 * @param stack the item stack
	 * @return the cooldown progress of the item, between 0 and 1
	 */
	protected float getCooldownProgress(ItemStack stack) {
		MinecraftClient client = MinecraftClient.getInstance();

		var player = client.player;
		if (player == null) {
			return 0;
		}

		return player.getItemCooldownManager().getCooldownProgress(stack.getItem(), client.getTickDelta());
	}

	@Override
	protected boolean isCooldownOverlayVisible(ItemStack stack) {
		return getCooldownProgress(stack) > 0;
	}

	@Override
	protected int getCooldownOverlayStep(ItemStack stack) {
		return (int) (getCooldownProgress(stack) * MAX_STEP);
	}

	@Override
	protected int getCooldownOverlayColor(ItemStack stack) {
		return 0x7FFFFFFF;
	}
}
