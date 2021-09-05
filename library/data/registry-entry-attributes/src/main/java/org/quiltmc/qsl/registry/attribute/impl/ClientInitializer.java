package org.quiltmc.qsl.registry.attribute.impl;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.ResourceType;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(new RegistryEntryAttributeReloader(true));
	}
}
