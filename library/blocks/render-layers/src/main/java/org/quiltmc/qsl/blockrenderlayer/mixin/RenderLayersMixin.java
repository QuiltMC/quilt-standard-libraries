package org.quiltmc.qsl.blockrenderlayer.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluid;
import org.quiltmc.qsl.blockrenderlayer.api.BlockRenderLayerMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RenderLayers.class)
public abstract class RenderLayersMixin {
	@Shadow @Final private static Map<Block, RenderLayer> BLOCKS;
	@Shadow @Final private static Map<Fluid, RenderLayer> FLUIDS;

	static {
		BlockRenderLayerMap.setMaps(BLOCKS, FLUIDS);
	}
}
