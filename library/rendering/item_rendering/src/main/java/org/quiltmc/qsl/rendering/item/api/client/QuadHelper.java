package org.quiltmc.qsl.rendering.item.api.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

@ApiStatus.NonExtendable
public interface QuadHelper {
	/**
	 * Gets the underlying {@code BufferBuilder}.
	 * <p>
	 * Note that the renderer will be reset when calling this method!
	 *
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder getBufferBuilder();

	/**
	 * Reinitialized the rendering state set by this helper.
	 * <p>
	 * Call this method after changing the rendering state!
	 */
	@Environment(EnvType.CLIENT)
	void reinitialize();

	/**
	 * Flushes the helper, drawing all currently batched quads to the screen.
	 */
	@Environment(EnvType.CLIENT)
	void flush();

	/**
	 * Begins building a buffer using the
	 * {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_COLOR POSITION_COLOR} vertex format.
	 *
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder beginQuad();
	/**
	 * Begins building a textured buffer using the
	 * {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR} vertex format.
	 *
	 * @param texture the identifier of the texture to use for the quad
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder beginTexturedQuad(@NotNull Identifier texture);
}
