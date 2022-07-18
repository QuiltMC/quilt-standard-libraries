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
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public interface GuiRendererHelper {
	@Environment(EnvType.CLIENT)
	default void renderGuiQuad(MatrixStack matrices,
							   BufferBuilder buffer, int x, int y, int width, int height, int color) {
		var mat = matrices.peek().getPosition();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = (color >> 24) & 0xFF;

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		buffer.vertex(mat, x, y, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y, 0.0f).color(r, g, b, a).next();
		BufferRenderer.drawWithShader(buffer.end());
	}

	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices,
									   BufferBuilder buffer, int x, int y, int width, int height,
									   Identifier texture, int u, int v, int texWidth, int texHeight,
									   int color) {
		var mat = matrices.peek().getPosition();

		RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f,
				((color >> 8) & 0xFF) / 255f,
				(color & 0xFF) / 255f,
				((color >> 24) & 0xFF) / 255f);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(mat, x, y, 0.0f).uv(u, v).next();
		buffer.vertex(mat, x, y + height, 0.0f).uv(u, v + texHeight).next();
		buffer.vertex(mat, x + width, y + height, 0.0f).uv(u + texWidth, v + texHeight).next();
		buffer.vertex(mat, x + width, y, 0.0f).uv(u + texWidth, v).next();
		BufferRenderer.drawWithShader(buffer.end());
	}

	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices,
									   BufferBuilder buffer, int x, int y, int width, int height,
									   Identifier texture, int u, int v, int texWidth, int texHeight) {
		renderGuiTexturedQuad(matrices, buffer, x, y, width, height, texture, u, v, texWidth, texHeight,
				0xFFFFFFFF);
	}

	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices,
									   BufferBuilder buffer, int x, int y, int width, int height,
									   Identifier texture, int u, int v,
									   int color) {
		renderGuiTexturedQuad(matrices, buffer, x, y, width, height, texture, u, v, width, height, color);
	}

	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices,
									   BufferBuilder buffer, int x, int y, int width, int height,
									   Identifier texture, int u, int v) {
		renderGuiTexturedQuad(matrices, buffer, x, y, width, height, texture, u, v, width, height,
				0xFFFFFFFF);
	}
}
