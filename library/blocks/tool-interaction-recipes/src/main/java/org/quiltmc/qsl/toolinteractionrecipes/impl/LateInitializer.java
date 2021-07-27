package org.quiltmc.qsl.toolinteractionrecipes.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;

// this is client/server instead of main so it runs after everyone's registered their recipes (hopefully)
// TODO remove this for data-driven impl
public class LateInitializer implements ClientModInitializer, DedicatedServerModInitializer {
	@Override
	public void onInitializeClient() {
		ToolInteractionRecipeCollections.update();
	}

	@Override
	public void onInitializeServer() {
		ToolInteractionRecipeCollections.update();
	}
}
