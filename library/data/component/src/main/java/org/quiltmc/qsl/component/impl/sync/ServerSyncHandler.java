/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.impl.sync;

import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.component.impl.sync.packet.RegistryPacket;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public final class ServerSyncHandler {
	private static ServerSyncHandler INSTANCE = null;

	private ServerSyncHandler() { }

	public static ServerSyncHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServerSyncHandler();
		}

		return INSTANCE;
	}

	public void registerChannel(SyncChannel<?, ?> syncChannel) {
		ComponentsImpl.LOGGER.info("Registering server-side component sync channel with id {}", syncChannel.getChannelId());
		ServerPlayNetworking.registerGlobalReceiver(syncChannel.getChannelId(), (server, sender, handler, buf, responseSender) ->
				syncChannel.handleClientSyncRequest(server, sender, buf)
		);
	}

	public void registerPackets() {
		ServerLoginNetworking.registerGlobalReceiver(PacketIds.TYPES, (server, handler, understood, buf, sync, sender) ->
				RegistryPacket.handleRegistryResponse(buf, handler, "Component with id %s was not found in the client!")
		);

		SyncChannel.createPacketChannels(this::registerChannel);
	}
}
