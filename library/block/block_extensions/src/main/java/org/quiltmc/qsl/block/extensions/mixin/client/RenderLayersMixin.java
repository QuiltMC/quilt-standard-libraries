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

package org.quiltmc.qsl.block.extensions.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluid;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.extensions.impl.client.BlockRenderLayerMapImpl;

@ClientOnly
@Mixin(RenderLayers.class)
public abstract class RenderLayersMixin {
	@Shadow
	@Final
	private static Map<Block, RenderLayer> BLOCKS;
	@Shadow
	@Final
	private static Map<Fluid, RenderLayer> FLUIDS;

	static {
		BlockRenderLayerMapImpl.initialize(
				(block, renderLayer) -> BLOCKS.put(block, renderLayer),
				(fluid, renderLayer) -> FLUIDS.put(fluid, renderLayer)
		);
	}
}
