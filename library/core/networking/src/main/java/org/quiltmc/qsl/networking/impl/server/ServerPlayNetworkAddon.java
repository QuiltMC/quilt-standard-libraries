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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.S2CPlayChannelEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.mixin.accessor.CustomPayloadC2SPacketAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.ServerPlayNetworkHandlerAccessor;

@ApiStatus.Internal
public final class ServerPlayNetworkAddon extends AbstractChanneledNetworkAddon<ServerPlayNetworking.ChannelReceiver> {
	private final ServerPlayNetworkHandler handler;
	private final MinecraftServer server;
	private boolean sentInitialRegisterPacket;

	public ServerPlayNetworkAddon(ServerPlayNetworkHandler handler, MinecraftServer server) {
		super(ServerNetworkingImpl.PLAY, ((ServerPlayNetworkHandlerAccessor) handler).getConnection(), "ServerPlayNetworkAddon for " + handler.player.getEntityName());
		this.handler = handler;
		this.server = server;

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection);

		// Register global receivers and attach to session
		this.receiver.startSession(this);
	}

	@Override
	public void lateInit() {
		for (Map.Entry<Identifier, ServerPlayNetworking.ChannelReceiver> entry : this.receiver.getReceivers().entrySet()) {
			this.registerChannel(entry.getKey(), entry.getValue());
		}

		ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.server);
	}

	public void onClientReady() {
		ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	/**
	 * Handles an incoming packet.
	 *
	 * @param packet the packet to handle
	 * @return true if the packet has been handled
	 */
	public boolean handle(CustomPayloadC2SPacket packet) {
		// Do not handle the packet on game thread
		if (this.server.isOnThread()) {
			return false;
		}

		CustomPayloadC2SPacketAccessor access = (CustomPayloadC2SPacketAccessor) packet;
		return this.handle(access.getChannel(), access.getData());
	}

	@Override
	protected void receive(ServerPlayNetworking.ChannelReceiver handler, PacketByteBuf buf) {
		handler.receive(this.server, this.handler.player, this.handler, buf, this);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.handler.player.server.execute(task);
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
			final PacketByteBuf buf = this.createRegistrationPacket(Collections.singleton(channelName));

			if (buf != null) {
				this.sendPacket(NetworkingImpl.REGISTER_CHANNEL, buf);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.sentInitialRegisterPacket) {
			final PacketByteBuf buf = this.createRegistrationPacket(Collections.singleton(channelName));

			if (buf != null) {
				this.sendPacket(NetworkingImpl.UNREGISTER_CHANNEL, buf);
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
		return NetworkingImpl.isReservedPlayChannel(channelName);
	}
}
