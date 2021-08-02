/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.block.extensions.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * Provides methods to set the {@link RenderLayer} of blocks and fluids.
 */
@Environment(EnvType.CLIENT)
public final class BlockRenderLayerMap {
	private static Map<Block, RenderLayer> blocks;
	private static Map<Fluid, RenderLayer> fluids;

	private BlockRenderLayerMap() { }

	/**
	 * Sets the render layer of the specified block.
	 *
	 * @param block target block
	 * @param layer new render layer
	 */
	public static void put(Block block, RenderLayer layer) {
		blocks.put(block, layer);
	}

	/**
	 * Sets the render layer of the specified blocks.
	 *
	 * @param layer new render layer
	 * @param blocks target blocks
	 */
	public static void put(RenderLayer layer, Block... blocks) {
		for (Block block : blocks)
			put(block, layer);
	}

	/**
	 * Sets the render layer of the specified fluid.
	 *
	 * @param fluid target fluid
	 * @param layer new render layer
	 */
	public static void put(Fluid fluid, RenderLayer layer) {
		fluids.put(fluid, layer);
	}

	/**
	 * Sets the render layer of the specified fluids.
	 *
	 * @param layer new render layer
	 * @param fluids target fluids
	 */
	public static void put(RenderLayer layer, Fluid... fluids) {
		for (Fluid fluid : fluids)
			put(fluid, layer);
	}

	@ApiStatus.Internal
	public static void initialize(Map<Block, RenderLayer> blocksIn, Map<Fluid, RenderLayer> fluidsIn) {
		blocks = blocksIn;
		fluids = fluidsIn;
	}
}
