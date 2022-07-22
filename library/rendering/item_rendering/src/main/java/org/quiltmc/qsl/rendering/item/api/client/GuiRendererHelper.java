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
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Contains various helper methods for rendering GUI elements.
 */
public interface GuiRendererHelper {
	/**
	 * Renders a solid-colored quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	static void renderQuad(MatrixStack matrices, BufferBuilder buffer,
						   int x, int y, int width, int height,
						   int color) {
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

	/**
	 * Renders a textured and tinted quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param regionWidth the width of the texture region
	 * @param regionHeight the height of the texture region
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	static void renderTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int regionWidth, int regionHeight,
								   int textureWidth, int textureHeight,
								   int color) {
		var mat = matrices.peek().getPosition();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = (color >> 24) & 0xFF;

		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		buffer.vertex(mat, x, y, 0.0f)
				.uv(u / (float) textureWidth, v / (float) textureHeight)
				.color(r, g, b, a).next();
		buffer.vertex(mat, x, y + height, 0.0f)
				.uv(u / (float) textureWidth, (v + regionHeight) / (float) textureHeight)
				.color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y + height, 0.0f)
				.uv((u + regionWidth) / (float) textureWidth, (v + regionHeight) / (float) textureHeight)
				.color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y, 0.0f)
				.uv((u + regionWidth) / (float) textureWidth, v / (float) textureHeight)
				.color(r, g, b, a).next();
		BufferRenderer.drawWithShader(buffer.end());
	}

	/**
	 * Renders a textured quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param regionWidth the width of the texture region
	 * @param regionHeight the height of the texture region
	 */
	@Environment(EnvType.CLIENT)
	static void renderTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int regionWidth, int regionHeight,
								   int textureWidth, int textureHeight) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, regionWidth, regionHeight,
				textureWidth, textureHeight,
				0xFFFFFFFF);
	}

	/**
	 * Renders a textured and tinted quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	static void renderTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v,
								   int textureWidth, int textureHeight,
								   int color) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				color);
	}

	/**
	 * Renders a textured quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 */
	@Environment(EnvType.CLIENT)
	static void renderTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v,
								   int textureWidth, int textureHeight) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				0xFFFFFFFF);
	}

	/**
	 * Renders a solid-colored quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	default void renderGuiQuad(MatrixStack matrices, BufferBuilder buffer,
							   int x, int y, int width, int height,
							   int color) {
		renderQuad(matrices, buffer, x, y, width, height, color);
	}

	/**
	 * Renders a textured and tinted quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param regionWidth the width of the texture region
	 * @param regionHeight the height of the texture region
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
									   int x, int y, int width, int height,
									   int u, int v, int regionWidth, int regionHeight,
									   int textureWidth, int textureHeight,
									   int color) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, regionWidth, regionHeight,
				textureWidth, textureHeight,
				color);
	}

	/**
	 * Renders a textured quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param regionWidth the width of the texture region
	 * @param regionHeight the height of the texture region
	 */
	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
									   int x, int y, int width, int height,
									   int u, int v, int regionWidth, int regionHeight,
									   int textureWidth, int textureHeight) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, regionWidth, regionHeight,
				textureWidth, textureHeight,
				0xFFFFFFFF);
	}

	/**
	 * Renders a textured and tinted quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 * @param color the color of the quad
	 */
	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
									   int x, int y, int width, int height,
									   int u, int v,
									   int textureWidth, int textureHeight,
									   int color) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				color);
	}

	/**
	 * Renders a textured quad to the screen.
	 *
	 * @param matrices the matrices
	 * @param buffer the buffer builder
	 * @param x the X position of the quad
	 * @param y the Y position of the quad
	 * @param width the width of the quad
	 * @param height the height of the quad
	 * @param u the X position of the texture region
	 * @param v the Y position of the texture region
	 */
	@Environment(EnvType.CLIENT)
	default void renderGuiTexturedQuad(MatrixStack matrices, BufferBuilder buffer,
									   int x, int y, int width, int height,
									   int u, int v,
									   int textureWidth, int textureHeight) {
		renderTexturedQuad(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				0xFFFFFFFF);
	}

}
