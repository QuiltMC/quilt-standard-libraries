package org.quiltmc.qsl.networking.api.channel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;

public interface S2CNetworkChannel<T> extends NetworkChannel<T> {
	@Override
	default NetworkSide getSide() {
		return NetworkSide.CLIENTBOUND;
	}

	// TODO: Add environment annotation
	interface Handler {
		void clientHandle(MinecraftClient client, ServerPlayerEntity player, ClientPlayNetworkHandler handler);
	}
}
