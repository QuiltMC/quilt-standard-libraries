package org.quiltmc.qsl.block.extensions.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

@Environment(EnvType.CLIENT)
public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.put(RenderLayer.getTranslucent(), Initializer.BLOCK);
	}
}
