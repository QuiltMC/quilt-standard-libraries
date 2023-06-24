/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.block.extensions.api.client;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.extensions.impl.client.BlockRenderLayerMapImpl;

/**
 * Provides methods to set the {@link RenderLayer} of blocks and fluids.
 */
@ClientOnly
public final class BlockRenderLayerMap {
	private BlockRenderLayerMap() {}

	/**
	 * Sets the render layer of the specified blocks.
	 *
	 * @param layer  new render layer
	 * @param blocks target blocks
	 */
	public static void put(RenderLayer layer, Block... blocks) {
		for (var block : blocks) {
			BlockRenderLayerMapImpl.put(block, layer);
		}
	}

	/**
	 * Sets the render layer of the specified fluids.
	 *
	 * @param layer  new render layer
	 * @param fluids target fluids
	 */
	public static void put(RenderLayer layer, Fluid... fluids) {
		for (var fluid : fluids) {
			BlockRenderLayerMapImpl.put(fluid, layer);
		}
	}
}
