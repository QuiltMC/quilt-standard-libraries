package org.quiltmc.qsl.component.impl.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.networking.api.PacketSender;

@Environment(EnvType.CLIENT)
public final class ClientEventListener {
	public static void onClientReady(MinecraftClient ignored) {
		Components.REGISTRY.freeze();
	}

	public static void onServerJoin(ClientPlayNetworkHandler ignoredHandler, PacketSender ignoredSender, MinecraftClient client) {
		ClientSyncHandler.getInstance().unfreeze();
		ClientSyncHandler.getInstance().processQueued(client);

		// We also request sync for these two while we are at it.
		SyncChannel.LEVEL.requestSync(client);
		SyncChannel.WORLD.requestSync(client.world);
	}

	public static void onServerDisconnect(ClientPlayNetworkHandler ignoredHandler, MinecraftClient ignoredClient) {
		ClientSyncHandler.getInstance().freeze();
	}

	public static void onClientTick(MinecraftClient ignoredClient) {
		SyncChannel.LEVEL.sendMassRequests();
		SyncChannel.WORLD.sendMassRequests();
		SyncChannel.CHUNK.sendMassRequests();
		SyncChannel.ENTITY.sendMassRequests();
		SyncChannel.BLOCK_ENTITY.sendMassRequests();
	}
}
