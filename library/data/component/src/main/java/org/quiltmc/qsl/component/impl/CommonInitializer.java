package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.impl.sync.DefaultSyncPacketHeaders;
import org.quiltmc.qsl.component.impl.sync.ServerSyncHandler;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;

@ApiStatus.Internal
public final class CommonInitializer implements ModInitializer {

	@Override
	public void onInitialize(ModContainer mod) {
		DefaultSyncPacketHeaders.registerDefaults();
		ServerSyncHandler.getInstance().registerPackets();
		ServerLoginConnectionEvents.QUERY_START.register(id("component_sync"), ServerSyncHandler.getInstance());

		ServerLifecycleEvents.STARTING.register(
				id("freeze_component_registries"),
				server -> ComponentsImpl.freezeRegistries()
		);
	}

	public static @NotNull Identifier id(@NotNull String id) {
		return new Identifier(ComponentsImpl.MODID, id);
	}
}
