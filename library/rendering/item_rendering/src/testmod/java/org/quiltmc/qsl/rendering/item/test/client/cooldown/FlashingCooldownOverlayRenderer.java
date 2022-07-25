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

package org.quiltmc.qsl.rendering.item.test.client.cooldown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import org.quiltmc.qsl.rendering.item.api.client.CooldownOverlayRenderer;
import org.quiltmc.qsl.rendering.item.api.client.VanillaCooldownOverlayRenderer;

/**
 * Shows a full red, flashing overlay if there's more than 80% of the cooldown still remaining.
 */
@Environment(EnvType.CLIENT)
public class FlashingCooldownOverlayRenderer extends VanillaCooldownOverlayRenderer {
	public static final CooldownOverlayRenderer INSTANCE = new FlashingCooldownOverlayRenderer();

	@Override
	protected int getCooldownOverlayStep(ItemStack stack) {
		if (getCooldownProgress(stack) > 0.8) {
			return MAX_STEP;
		}

		return super.getCooldownOverlayStep(stack);
	}

	@Override
	protected int getCooldownOverlayColor(ItemStack stack) {
		// Between 1 and 0.5 color it red, otherwise use the default color
		if (getCooldownProgress(stack) > 0.8f) {
			float a = 0.1f + 0.7f * (float) Math.sin((Util.getMeasuringTimeMs() % 250) / 250.0f * (float) Math.PI);
			a = MathHelper.clamp(a, 0f, 1f);
			return 0xFF0000 | ((int)(a * 0xFF) << 24);
		}

		return super.getCooldownOverlayColor(stack);
	}
}
