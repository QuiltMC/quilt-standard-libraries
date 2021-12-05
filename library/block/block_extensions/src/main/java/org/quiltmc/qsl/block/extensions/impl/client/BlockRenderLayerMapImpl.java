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

package org.quiltmc.qsl.block.extensions.impl.client;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class BlockRenderLayerMapImpl {
	private BlockRenderLayerMapImpl() {
	}

	private static Map<Block, RenderLayer> blocks;
	private static Map<Fluid, RenderLayer> fluids;

	public static void initialize(Map<Block, RenderLayer> blocksIn, Map<Fluid, RenderLayer> fluidsIn) {
		blocks = blocksIn;
		fluids = fluidsIn;
	}

	public static void put(Block block, RenderLayer layer) {
		blocks.put(block, layer);
	}

	public static void put(Fluid fluid, RenderLayer layer) {
		fluids.put(fluid, layer);
	}
}
