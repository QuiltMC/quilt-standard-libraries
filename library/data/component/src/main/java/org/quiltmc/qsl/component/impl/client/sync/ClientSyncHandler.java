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
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class ClientSyncHandler {
	private static ClientSyncHandler INSTANCE = null;

	private IdList<ComponentType<?>> componentList = null;

	private ClientSyncHandler() { }

	public static ClientSyncHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClientSyncHandler();
		}

		return INSTANCE;
	}

	public <P extends ComponentProvider> void registerChannel(SyncChannel<P> channel) {
		ClientPlayNetworking.registerGlobalReceiver(channel.channelId(), (client, handler, buf, responseSender) -> {
			ComponentProvider provider = channel.to(buf);
			SyncPacket.handle(client, provider, buf);
		});
	}

	public void registerPackets() {
		ClientLoginNetworking.registerGlobalReceiver(PacketIds.TYPES, (client, handler, buf, listenerAdder) ->
				ClientRegistryPacket.handleRegistryPacket(buf, Components.REGISTRY, list -> this.componentList = list)
		);

		SyncChannel.createPacketChannels(this::registerChannel);
	}

	public ComponentType<?> getType(int rawId) {
		return this.componentList.get(rawId);
	}
}
