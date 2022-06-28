package org.quiltmc.qsl.component.impl.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.component.impl.sync.packet.RegistryPacket;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;

public final class ServerSyncHandler implements ServerLoginConnectionEvents.QueryStart {

	private static ServerSyncHandler INSTANCE = null;

	private ServerSyncHandler() {

	}

	public static ServerSyncHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServerSyncHandler();
		}

		return INSTANCE;
	}

	public void registerPackets() {
		ServerLoginNetworking.registerGlobalReceiver(PacketIds.TYPES, (server, handler, understood, buf, sync, sender) ->
				RegistryPacket.handleRegistryResponse(buf, handler, "Component with id %s was not found in the client!")
		);
		ServerLoginNetworking.registerGlobalReceiver(PacketIds.HEADERS, (server, handler, understood, buf, synchronizer, sender) ->
				RegistryPacket.handleRegistryResponse(buf, handler, "Header with id %s was not found in the client!")
		);
	}

	@Override
	public void onLoginStart(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
		sender.sendPacket(PacketIds.TYPES, RegistryPacket.createRegistryPacket(Components.REGISTRY));
		sender.sendPacket(PacketIds.HEADERS, RegistryPacket.createRegistryPacket(ComponentHeaderRegistry.HEADERS));
	}
}
