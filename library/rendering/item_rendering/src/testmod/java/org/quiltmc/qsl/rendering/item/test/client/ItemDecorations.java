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

package org.quiltmc.qsl.rendering.item.test.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.rendering.item.api.client.GuiRendererHelper;
import org.quiltmc.qsl.rendering.item.test.ItemRenderingTestmod;

@Environment(EnvType.CLIENT)
public class ItemDecorations {
	private ItemDecorations() { }

	public static final Identifier STACK_BORDER_TEXTURE = ItemRenderingTestmod.id("textures/gui/border.png");
	public static final Identifier WARNING_ICON_TEXTURE = ItemRenderingTestmod.id("textures/gui/warning.png");

	public static void renderStackBorder(MatrixStack matrices, int color) {
		RenderSystem.enableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, STACK_BORDER_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		GuiRendererHelper.renderTexturedQuad(matrices, buffer,
				-1, -1, 18, 18, 18, 18, 18, 18, color);
	}

	@SuppressWarnings("ConstantConditions")
	public static void renderStackBorder(MatrixStack matrices, Formatting color) {
		renderStackBorder(matrices, 0xFF000000 | color.getColorValue());
	}

	public static void renderWarningIcon(MatrixStack matrices) {
		RenderSystem.disableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, WARNING_ICON_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		GuiRendererHelper.renderTexturedQuad(matrices, buffer,
				-1, -1, 18, 18, 18, 18, 18, 18);
		RenderSystem.enableDepthTest();
	}
}
