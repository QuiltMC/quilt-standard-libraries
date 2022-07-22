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

import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.item.api.client.CooldownOverlayRenderer;
import org.quiltmc.qsl.rendering.item.api.client.VanillaCooldownOverlayRenderer;

/**
 * Hides the cooldown overlay, even if there is a cooldown, as long as it has more than 20% remaining.
 */
public class HiddenCooldownOverlayRenderer extends VanillaCooldownOverlayRenderer {
	public static final CooldownOverlayRenderer INSTANCE = new HiddenCooldownOverlayRenderer();

	@Override
	protected boolean isCooldownOverlayVisible(ItemStack stack) {
		return getCooldownProgress(stack) <= 0.2;
	}
}
