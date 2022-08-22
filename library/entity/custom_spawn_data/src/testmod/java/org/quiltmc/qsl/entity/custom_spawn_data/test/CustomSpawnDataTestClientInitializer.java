package org.quiltmc.qsl.entity.custom_spawn_data.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.custom_spawn_data.test.mixin.EntityModelLayersAccessor;
import org.quiltmc.qsl.entity.custom_spawn_data.test.mixin.EntityRenderersAccessor;

@Environment(EnvType.CLIENT)
public class CustomSpawnDataTestClientInitializer implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRenderersAccessor.quilt$register(CustomSpawnDataTestInitializer.BOX_TYPE, BoxEntityRenderer::new);
		EntityModelLayersAccessor.quilt$LAYERS().add(BoxModel.LAYER);
	}
}
