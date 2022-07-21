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

import java.util.ArrayDeque;
import java.util.Queue;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class ClientSyncHandler {
	private static ClientSyncHandler INSTANCE = null;
	private final Queue<Pair<SyncChannel<?, ?>, PacketByteBuf>> queue = new ArrayDeque<>();
	private IdList<ComponentType<?>> componentList = null;
	private boolean frozen = true;

	private ClientSyncHandler() { }

	public static ClientSyncHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClientSyncHandler();
		}

		return INSTANCE;
	}

	public void registerChannel(SyncChannel<?, ?> channel) {
		ComponentsImpl.LOGGER.info("Registering client-side component sync channel with id " + channel.getChannelId());
		ClientPlayNetworking.registerGlobalReceiver(channel.getChannelId(), (client, handler, buf, responseSender) -> {
			if (this.frozen) {
				buf.retain(); // we keep the buffer in memory
				this.queue.add(Pair.of(channel, buf));
			} else {
				channel.handleServerPushedSync(client, buf);
			}
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

	public void unfreeze() {
		this.frozen = false;
	}

	public void freeze() {
		this.frozen = true;
	}

	public void processQueued(MinecraftClient client) {
		while (!this.queue.isEmpty()) {
			Pair<SyncChannel<?, ?>, PacketByteBuf> currentPair = this.queue.poll();

			var channel = currentPair.getFirst();
			var buf = currentPair.getSecond();

			channel.handleServerPushedSync(client, buf);

			buf.release(); // release it once we are done
		}
	}
}
