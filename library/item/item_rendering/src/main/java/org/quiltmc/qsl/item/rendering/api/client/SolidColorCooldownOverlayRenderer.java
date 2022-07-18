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

package org.quiltmc.qsl.item.rendering.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public abstract class SolidColorCooldownOverlayRenderer implements CooldownOverlayRenderer {
	@Override
	public void renderCooldownOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
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

	protected abstract boolean isCooldownOverlayVisible(ItemStack stack);

	/**
	 * {@return the height of the cooldown overlay in pixels (out of 13)}
	 */
	protected abstract int getCooldownOverlayStep(ItemStack stack);

	protected abstract int getCooldownOverlayColor(ItemStack stack);
}
