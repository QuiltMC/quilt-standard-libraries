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

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

/**
 * Helper class to batch multiple quad draws together for improved performance.
 */
@ApiStatus.NonExtendable
public interface QuadBatchManager {
	/**
	 * Ends the current batch (if any) and returns the underlying {@code BufferBuilder}.
	 *
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder getBufferBuilder();

	/**
	 * Reinitializes the rendering state.
	 * <p>
	 * Call this method after changing the rendering state!
	 */
	@Environment(EnvType.CLIENT)
	void reinitialize();

	/**
	 * Ends the current batch, if any, and draws the quads to the screen.
	 */
	@Environment(EnvType.CLIENT)
	void endCurrentBatch();

	/**
	 * Begins, or continues, an untextured batch of quads.
	 * <p>
	 * The returned buffer will use the
	 * {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_COLOR POSITION_COLOR} vertex format.
	 *
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder beginQuads();

	/**
	 * Begins, or continues, a textured batch of quads using the specified texture.
	 * <p>
	 * The returned buffer will use the
	 * {@link com.mojang.blaze3d.vertex.VertexFormats#POSITION_TEXTURE_COLOR POSITION_TEXTURE_COLOR} vertex format.
	 *
	 * @param texture the identifier of the texture to use for the quad
	 * @return the buffer builder
	 */
	@Environment(EnvType.CLIENT)
	@NotNull BufferBuilder beginTexturedQuads(@NotNull Identifier texture);
}
