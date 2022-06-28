package org.quiltmc.qsl.component.impl.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

public class ClientInitializer implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientSyncHandler.getInstance().registerPackets();

		ClientLifecycleEvents.READY.register(
				CommonInitializer.id("freeze_component_registies"),
				client -> ComponentsImpl.freezeRegistries()
		);
	}
}
