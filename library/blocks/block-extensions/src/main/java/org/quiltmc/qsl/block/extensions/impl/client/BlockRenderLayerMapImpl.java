package org.quiltmc.qsl.block.extensions.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class BlockRenderLayerMapImpl {
	private BlockRenderLayerMapImpl() { }

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
