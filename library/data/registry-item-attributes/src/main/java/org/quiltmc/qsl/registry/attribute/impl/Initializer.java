package org.quiltmc.qsl.registry.attribute.impl;

import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import net.minecraft.resource.ResourceType;

public final class Initializer implements ModInitializer {
	@Override
	public void onInitialize() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new RegistryEntryAttributeReloader());
	}
}
