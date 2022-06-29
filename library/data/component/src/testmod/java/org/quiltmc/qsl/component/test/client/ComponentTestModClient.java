package org.quiltmc.qsl.component.test.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class ComponentTestModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
	}
}
