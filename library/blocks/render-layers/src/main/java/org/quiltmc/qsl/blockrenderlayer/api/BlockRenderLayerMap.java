package org.quiltmc.qsl.blockrenderlayer.api;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Provides methods to set {@link RenderLayer}s for custom blocks and fluids.
 */
public final class BlockRenderLayerMap {
	private BlockRenderLayerMap() { }

	/**
	 * Sets a block's render layer.
	 * @param block block
	 * @param layer new render layer
	 */
	public static void put(@NotNull Block block, @NotNull RenderLayer layer) {
		blocks.put(block, layer);
	}

	/**
	 * Sets a fluid's render layer.
	 * @param fluid fluid
	 * @param layer new render layer
	 */
	public static void put(@NotNull Fluid fluid, @NotNull RenderLayer layer) {
		fluids.put(fluid, layer);
	}

	/**
	 * Sets multiple blocks' render layer.
	 * @param layer new render layer
	 * @param blocks blocks
	 */
	public static void put(@NotNull RenderLayer layer, @NotNull Block... blocks) {
		for (Block block : blocks)
			put(block, layer);
	}

	/**
	 * Sets multiple fluids' render layer.
	 * @param layer new render layer
	 * @param fluids fluids
	 */
	public static void put(@NotNull RenderLayer layer, @NotNull Fluid... fluids) {
		for (Fluid fluid : fluids)
			put(fluid, layer);
	}

	private static Map<Block, RenderLayer> blocks;
	private static Map<Fluid, RenderLayer> fluids;

	@ApiStatus.Internal
	public static void setMaps(@NotNull Map<Block, RenderLayer> blocksIn, @NotNull Map<Fluid, RenderLayer> fluidsIn) {
		blocks = blocksIn;
		fluids = fluidsIn;
	}
}
