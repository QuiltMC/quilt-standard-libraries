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

package org.quiltmc.qsl.block.extensions.impl.client;

import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.ApiStatus;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.unmapped.C_nihubpux;

import org.quiltmc.loader.api.minecraft.ClientOnly;

@ApiStatus.Internal
@ClientOnly
public final class BlockRenderLayerMapImpl {
	private BlockRenderLayerMapImpl() {
	}

	private static Map<Block, C_nihubpux> blockMap = new Reference2ReferenceOpenHashMap<>();
	private static Map<Fluid, C_nihubpux> fluidMap = new Reference2ReferenceOpenHashMap<>();

	private static BiConsumer<Block, C_nihubpux> blockHandler = blockMap::put;
	private static BiConsumer<Fluid, C_nihubpux> fluidHandler = fluidMap::put;

	public static void initialize(BiConsumer<Block, C_nihubpux> blockHandlerIn, BiConsumer<Fluid, C_nihubpux> fluidHandlerIn) {
		// Add pre-existing render layer assignments.
		if (blockMap != null) {
			blockMap.forEach(blockHandlerIn);
		}

		if (fluidMap != null) {
			fluidMap.forEach(fluidHandlerIn);
		}

		// Set handlers to directly accept later additions.
		blockHandler = blockHandlerIn;
		fluidHandler = fluidHandlerIn;

		// Lose the maps, let the GC take care of them.
		blockMap = null;
		fluidMap = null;
	}

	public static void put(Block block, C_nihubpux layer) {
		blockHandler.accept(block, layer);
	}

	public static void put(Fluid fluid, C_nihubpux layer) {
		fluidHandler.accept(fluid, layer);
	}
}
