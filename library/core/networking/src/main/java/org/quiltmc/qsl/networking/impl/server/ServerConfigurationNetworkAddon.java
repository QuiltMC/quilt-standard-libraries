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
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.S2CConfigurationChannelEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.ServerConfigurationPacketHandlerAccessor;

@ApiStatus.Internal
public final class ServerConfigurationNetworkAddon extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.CustomChannelReceiver<?>> {
	private final ServerConfigurationPacketHandler handler;
	private final MinecraftServer server;
	private boolean sentInitialRegisterPacket = false;

	public ServerConfigurationNetworkAddon(ServerConfigurationPacketHandler handler, MinecraftServer server) {
		super(ServerNetworkingImpl.CONFIGURATION, ((AbstractServerPacketHandlerAccessor) handler).getConnection(), "ServerConfigurationNetworkAddon for " + handler.getHost().getName());
		this.handler = handler;
		this.server = server;

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkState.CONFIGURATION);

		// Register global receivers and attach to session
		this.receiver.startSession(this);
	}

	@Override
	public void lateInit() {
		for (Map.Entry<Identifier, ServerConfigurationNetworking.CustomChannelReceiver<?>> entry : this.receiver.getReceivers().entrySet()) {
			this.registerChannel(entry.getKey(), entry.getValue());
		}

		ServerConfigurationConnectionEvents.INIT.invoker().onConfigurationInit(this.handler, this.server);
	}

	public void onConfigureReady() {
		ServerConfigurationConnectionEvents.READY.invoker().onConfigurationReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	@Override
	public <T extends CustomPayload> boolean handle(T payload) {
		boolean handled = super.handle(payload);
		if (handled && payload.id().equals(NetworkingImpl.REGISTER_CHANNEL)) {
			((ServerConfigurationPacketHandlerAccessor) this.handler).invokeFinishCurrentTask(SendChannelsTask.TYPE);
		}

		return handled;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends CustomPayload> void receive(ServerConfigurationNetworking.CustomChannelReceiver<?> handler, T buf) {
		((ServerConfigurationNetworking.CustomChannelReceiver<T>) handler).receive(this.server, this.handler, buf, this);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.server.execute(task);
	}

	@Override
	public Packet<?> createPacket(CustomPayload payload) {
		return ServerConfigurationNetworking.createS2CPacket(payload);
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ServerConfigurationNetworking.createS2CPacket(channelName, buf);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		S2CConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		S2CConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), false);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(this.handler, this.server);
		this.receiver.endSession(this);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) ((AbstractServerPacketHandlerAccessor) this.handler).getConnection();
	}
}
