package org.quiltmc.qsl.registry.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.registry.impl.sync.ClientRegistrySync;

@Environment(EnvType.CLIENT)
public class RegistryLibInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientRegistrySync.registerHandlers();
	}
}
