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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * A {@link CooldownOverlayRenderer} implementation that renders a quad with a solid color over the item.
 */
public abstract class SolidColorCooldownOverlayRenderer implements CooldownOverlayRenderer {
	/**
	 * The maximum return value of {@link #getCooldownOverlayStep(ItemStack)}.
	 * <p>
	 * Higher return values are <em>unsupported</em>, and may cause rendering errors.
	 */
	public static final int MAX_STEP = 16;

	@Override
	public void renderCooldownOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
		if (!isCooldownOverlayVisible(stack)) {
			return;
		}

		int step = getCooldownOverlayStep(stack);
		int color = getCooldownOverlayColor(stack);

		RenderSystem.disableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		this.renderGuiQuad(matrices, buffer, 0, 16 - step, 16, step, color);
		RenderSystem.enableTexture();
		RenderSystem.enableDepthTest();
	}

	/**
	 * Checks if the cooldown overlay should be rendered for the given {@code ItemStack}.
	 *
	 * @param stack the item stack
	 * @return {@code true} if cooldown overlay should be rendered, {@code false} otherwise
	 */
	protected boolean isCooldownOverlayVisible(ItemStack stack) {
		return getCooldownOverlayStep(stack) > 0;
	}

	/**
	 * Gets the height of the cooldown overlay, in pixels, for the given {@code ItemStack}.
	 *
	 * @param stack the item stack
	 * @return the height of the cooldown overlay
	 * @see #MAX_STEP
	 */
	protected abstract int getCooldownOverlayStep(ItemStack stack);

	/**
	 * Gets the color of the cooldown overlay, in ARGB format, for the given {@code ItemStack}.
	 *
	 * @param stack the item stack
	 * @return the color of the cooldown overlay
	 */
	protected abstract int getCooldownOverlayColor(ItemStack stack);
}
