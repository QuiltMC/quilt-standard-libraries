package org.quiltmc.qsl.registry.attribute.impl;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.ResourceType;

public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RegistryEntryAttributeReloader.register(ResourceType.CLIENT_RESOURCES);
	}
}
