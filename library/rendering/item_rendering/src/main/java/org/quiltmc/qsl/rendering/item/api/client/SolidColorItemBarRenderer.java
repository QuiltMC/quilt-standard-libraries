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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class SolidColorItemBarRenderer implements ItemBarRenderer {
	public static final int MAX_STEP = 13;

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return getItemBarStep(stack) > 0;
	}

	@Override
	public void renderItemBar(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
		RenderSystem.disableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		this.renderGuiQuad(matrices, buffer, 2, 0, 13, 2, getItemBarBackground(stack));
		this.renderGuiQuad(matrices, buffer, 2, 0, getItemBarStep(stack), 1, getItemBarForeground(stack));
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
		RenderSystem.enableDepthTest();
	}

	protected int getItemBarBackground(ItemStack stack) {
		return 0xFF000000;
	}

	/**
	 * {@return the length of the filled section of the durability bar in pixels (out of 13)}
	 */
	protected abstract int getItemBarStep(ItemStack stack);

	protected abstract int getItemBarForeground(ItemStack stack);
}
