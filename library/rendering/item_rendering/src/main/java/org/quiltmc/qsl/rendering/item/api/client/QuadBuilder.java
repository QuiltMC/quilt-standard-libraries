package org.quiltmc.qsl.rendering.item.api.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Contains various helper methods for building quad elements in a {@link BufferBuilder}.
 */
@Environment(EnvType.CLIENT)
public final class QuadBuilder {
	private QuadBuilder() {
		throw new RuntimeException("QuadBuilder only contains static declarations.");
	}

	/**
	 * The color white, completely opaque, in ARGB format.
	 */
	public static final int WHITE = 0xFFFFFFFF;

	/**
	 * Adds an untextured quad to the specified {@code BufferBuilder}.
	 * <p>
	 * The builder must be in the {@link com.mojang.blaze3d.vertex.VertexFormat.DrawMode#QUADS QUADS} draw mode,
	 * and must use the {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_COLOR POSITION_COLOR} vertex format.
	 *
	 * @param matrices the matrices
	 * @param buffer   the buffer builder
	 * @param x        the X coordinate of the top-left corner of the quad
	 * @param y        the Y coordinate of the top-left corner of the quad
	 * @param width    the width of the quad
	 * @param height   the height of the quad
	 * @param color    the color of the quad, in ARGB format
	 */
	public static void add(MatrixStack matrices, BufferBuilder buffer,
						   int x, int y, int width, int height,
						   int color) {
		var mat = matrices.peek().getPosition();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = (color >> 24) & 0xFF;

		buffer.vertex(mat, x, y, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y, 0.0f).color(r, g, b, a).next();
	}

	/**
	 * Adds a textured quad to the specified {@code BufferBuilder}.
	 * <p>
	 * The builder must be in the {@link com.mojang.blaze3d.vertex.VertexFormat.DrawMode#QUADS QUADS} draw mode,
	 * and must use the {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR}
	 * vertex format.
	 *
	 * @param matrices      the matrices
	 * @param buffer        the buffer builder
	 * @param x             the X coordinate of the top-left corner of the quad
	 * @param y             the Y coordinate of the top-left corner of the quad
	 * @param width         the width of the quad
	 * @param height        the height of the quad
	 * @param u             the X coordinate of the top-left corner of the texture region
	 * @param v             the Y coordinate of the top-left corner of the texture region
	 * @param regionWidth   the width of the texture region
	 * @param regionHeight  the height of the texture region
	 * @param textureWidth  the width of the entire texture
	 * @param textureHeight the height of the entire texture
	 * @param color         the color of the quad, in ARGB format
	 */
	public static void addTextured(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int regionWidth, int regionHeight,
								   int textureWidth, int textureHeight,
								   int color) {
		var mat = matrices.peek().getPosition();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = (color >> 24) & 0xFF;

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
	}

	/**
	 * Adds a textured quad to the specified {@code BufferBuilder}.
	 * <p>
	 * The builder must be in the {@link com.mojang.blaze3d.vertex.VertexFormat.DrawMode#QUADS QUADS} draw mode,
	 * and must use the {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR}
	 * vertex format.
	 *
	 * @param matrices      the matrices
	 * @param buffer        the buffer builder
	 * @param x             the X coordinate of the top-left corner of the quad
	 * @param y             the Y coordinate of the top-left corner of the quad
	 * @param width         the width of the quad
	 * @param height        the height of the quad
	 * @param u             the X coordinate of the top-left corner of the texture region
	 * @param v             the Y coordinate of the top-left corner of the texture region
	 * @param regionWidth   the width of the texture region
	 * @param regionHeight  the height of the texture region
	 * @param textureWidth  the width of the entire texture
	 * @param textureHeight the height of the entire texture
	 */
	public static void addTextured(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int regionWidth, int regionHeight,
								   int textureWidth, int textureHeight) {
		addTextured(matrices, buffer,
				x, y, width, height,
				u, v, regionWidth, regionHeight,
				textureWidth, textureHeight,
				WHITE);
	}

	/**
	 * Adds a textured quad to the specified {@code BufferBuilder}.
	 * <p>
	 * The builder must be in the {@link com.mojang.blaze3d.vertex.VertexFormat.DrawMode#QUADS QUADS} draw mode,
	 * and must use the {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR}
	 * vertex format.
	 *
	 * @param matrices      the matrices
	 * @param buffer        the buffer builder
	 * @param x             the X coordinate of the top-left corner of the quad
	 * @param y             the Y coordinate of the top-left corner of the quad
	 * @param width         the width of the quad
	 * @param height        the height of the quad
	 * @param u             the X coordinate of the top-left corner of the texture region
	 * @param v             the Y coordinate of the top-left corner of the texture region
	 * @param textureWidth  the width of the entire texture
	 * @param textureHeight the height of the entire texture
	 * @param color         the color of the quad, in ARGB format
	 */
	public static void addTextured(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int textureWidth, int textureHeight,
								   int color) {
		addTextured(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				color);
	}

	/**
	 * Adds a textured quad to the specified {@code BufferBuilder}.
	 * <p>
	 * The builder must be in the {@link com.mojang.blaze3d.vertex.VertexFormat.DrawMode#QUADS QUADS} draw mode,
	 * and must use the {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR}
	 * vertex format.
	 *
	 * @param matrices      the matrices
	 * @param buffer        the buffer builder
	 * @param x             the X coordinate of the top-left corner of the quad
	 * @param y             the Y coordinate of the top-left corner of the quad
	 * @param width         the width of the quad
	 * @param height        the height of the quad
	 * @param u             the X coordinate of the top-left corner of the texture region
	 * @param v             the Y coordinate of the top-left corner of the texture region
	 * @param textureWidth  the width of the entire texture
	 * @param textureHeight the height of the entire texture
	 */
	public static void addTextured(MatrixStack matrices, BufferBuilder buffer,
								   int x, int y, int width, int height,
								   int u, int v, int textureWidth, int textureHeight) {
		addTextured(matrices, buffer,
				x, y, width, height,
				u, v, width, height,
				textureWidth, textureHeight,
				WHITE);
	}
}
