/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.networking.impl.server;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.S2CPlayChannelEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;

@ApiStatus.Internal
public final class ServerPlayNetworkAddon extends AbstractChanneledNetworkAddon<ServerPlayNetworking.CustomChannelReceiver<?>> {
	private final ServerPlayNetworkHandler handler;
	private final MinecraftServer server;
	private boolean sentInitialRegisterPacket;

	public ServerPlayNetworkAddon(ServerPlayNetworkHandler handler, MinecraftServer server) {
		super(ServerNetworkingImpl.PLAY, ((AbstractServerPacketHandlerAccessor) handler).getConnection(), "ServerPlayNetworkAddon for " + handler.player.getProfileName());
		this.handler = handler;
		this.server = server;

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkState.PLAY);

		// Register global receivers and attach to session
		this.receiver.startSession(this);
	}

	@Override
	public void lateInit() {
		for (Map.Entry<Identifier, ServerPlayNetworking.CustomChannelReceiver<?>> entry : this.receiver.getReceivers().entrySet()) {
			this.registerChannel(entry.getKey(), entry.getValue());
		}

		ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.server);
	}

	public void onClientReady() {
		ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends CustomPayload> void receive(ServerPlayNetworking.CustomChannelReceiver<?> handler, T buf) {
		((ServerPlayNetworking.CustomChannelReceiver<T>) handler).receive(this.server, this.handler.player, this.handler, buf, this);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.handler.player.server.execute(task);
	}

	@Override
	public Packet<?> createPacket(CustomPayload payload) {
		return ServerNetworkingImpl.createS2CPacket(payload);
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ServerPlayNetworking.createS2CPacket(channelName, buf);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), false);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.handler, this.server);
		this.receiver.endSession(this);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}
}
