package org.quiltmc.qsl.component.impl.client.sync;

import net.minecraft.util.collection.IdList;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.sync.ComponentHeaderRegistry;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class ClientSyncHandler {

	private static ClientSyncHandler INSTANCE = null;
	private IdList<ComponentType<?>> componentList = null;
	private IdList<SyncPacketHeader<?>> headerList = null;

	public static ClientSyncHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClientSyncHandler();
		}

		return INSTANCE;
	}

	public void registerPackets() {
		ClientLoginNetworking.registerGlobalReceiver(PacketIds.TYPES, (client, handler, buf, listenerAdder) ->
				ClientRegistryPacket.handleRegistryPacket(buf, Components.REGISTRY, list -> this.componentList = list)
		);

		ClientLoginNetworking.registerGlobalReceiver(PacketIds.HEADERS, (client, handler, buf, listenerAdder) ->
				ClientRegistryPacket.handleRegistryPacket(buf, ComponentHeaderRegistry.HEADERS, list -> this.headerList = list)
		);
		ClientPlayNetworking.registerGlobalReceiver(PacketIds.SYNC, (client, handler, buf, responseSender) ->
				SyncPacket.handle(client, buf)
		);
	}

	public SyncPacketHeader<?> getHeader(int rawId) {
		return this.headerList.get(rawId);
	}

	public ComponentType<?> getType(int rawId) {
		return this.componentList.get(rawId);
	}
}
