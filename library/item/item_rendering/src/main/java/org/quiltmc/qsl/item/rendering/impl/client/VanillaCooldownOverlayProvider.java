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

package org.quiltmc.qsl.item.rendering.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.rendering.api.client.CooldownOverlayProvider;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class VanillaCooldownOverlayProvider implements CooldownOverlayProvider {
	public static final CooldownOverlayProvider INSTANCE = new VanillaCooldownOverlayProvider();

	private VanillaCooldownOverlayProvider() { }

	private final MinecraftClient client = MinecraftClient.getInstance();

	private float getCooldown(ItemStack stack) {
		var player = client.player;
		if (player == null) {
			return 0;
		}
		return player.getItemCooldownManager().getCooldownProgress(stack.getItem(), client.getTickDelta());
	}

	@Override
	public boolean isCooldownOverlayVisible(ItemStack stack) {
		return getCooldown(stack) > 0;
	}

	@Override
	public int getCooldownOverlayStep(ItemStack stack) {
		return (int) (getCooldown(stack) * 16);
	}

	@Override
	public int getCooldownOverlayColor(ItemStack stack) {
		return 0x7FFFFFFF;
	}
}
