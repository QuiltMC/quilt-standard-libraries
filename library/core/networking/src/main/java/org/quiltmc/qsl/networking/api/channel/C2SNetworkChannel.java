package org.quiltmc.qsl.networking.api.channel;

import net.minecraft.network.NetworkSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public interface C2SNetworkChannel<T> extends NetworkChannel<T> {
	// TODO: Add environment annotations
	default void send(T t) {
		ClientPlayNetworking.send(this.getId(), this.getCodec().createBuffer(t));
	}

	@Override
	default NetworkSide getSide() {
		return NetworkSide.SERVERBOUND;
	}

	interface Handler {
		void serverHandle(
				MinecraftServer server,
				ServerPlayerEntity player,
				ServerPlayNetworkHandler handler,
				PacketSender responseSender
		);
	}
}
