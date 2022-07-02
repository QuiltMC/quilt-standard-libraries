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

package org.quiltmc.qsl.component.impl.client.sync;

import net.minecraft.util.collection.IdList;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.sync.SyncHeaderRegistry;
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
				ClientRegistryPacket.handleRegistryPacket(buf, SyncHeaderRegistry.HEADERS, list -> this.headerList = list)
		);
		ClientPlayNetworking.registerGlobalReceiver(PacketIds.SYNC, (client, handler, buf, responseSender) ->
				SyncPacket.handle(buf, client)
		);
	}

	public SyncPacketHeader<?> getHeader(int rawId) {
		return this.headerList.get(rawId);
	}

	public ComponentType<?> getType(int rawId) {
		return this.componentList.get(rawId);
	}
}
