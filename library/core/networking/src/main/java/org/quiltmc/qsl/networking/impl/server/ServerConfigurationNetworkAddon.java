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
import org.quiltmc.qsl.networking.api.S2CConfigurationChannelEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.client.ClientNetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.PingS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public final class ServerConfigurationNetworkAddon extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.CustomChannelReceiver<?>> {
	private final ServerConfigurationPacketHandler handler;
	private final MinecraftServer server;
	private RegisterState registerState = RegisterState.NOT_SENT;

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
	}

	public void preConfiguration() {
		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.invoker().onConfiguration(this.handler, this.server);
	}

	public void configuration() {
		ServerConfigurationConnectionEvents.CONFIGURE.invoker().onConfiguration(this.handler, this.server);
	}

	public boolean startConfiguration() {
		if (this.registerState == RegisterState.NOT_SENT) {
			// Send the registration packet, followed by a ping
			this.sendInitialChannelRegistrationPacket();
			this.sendPacket(new PingS2CPacket(0x04117));

			this.registerState = RegisterState.SENT;

			// Cancel the configuration for now, the response from the ping or registration packet will continue.
			return true;
		}

		// We should have received a response
		assert this.registerState == RegisterState.RECEIVED || this.registerState == RegisterState.NOT_RECEIVED;
		return false;
	}

	@Override
	protected void receiveRegistration(boolean register, ChannelPayload payload) {
		super.receiveRegistration(register, payload);

		if (register && this.registerState == RegisterState.SENT) {
			// We received the registration packet, thus we know this is a modded client, continue with configuration.
			this.registerState = RegisterState.RECEIVED;
			this.handler.startConfiguration();
		}
	}

	public void onPong(int parameter) {
		if (this.registerState == RegisterState.SENT) {
			// We did not receive the registration packet, thus we think this is a vanilla client, continue with configuration.
			this.registerState = RegisterState.NOT_RECEIVED;
			this.handler.startConfiguration();
		}
	}

	/**
	 * Handles an incoming packet.
	 *
	 * @param payload the payload to handle
	 * @return true if the packet has been handled
	 */
	public boolean handle(PacketByteBufPayload payload) {
		return super.handle(payload);
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
	protected Packet<?> createPacket(CustomPayload payload) {
		return ServerNetworkingImpl.createS2CPacket(payload);
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ServerPlayNetworking.createS2CPacket(channelName, buf);
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
		// If we can already send packets, immediately send the register packet for this channel
		if (this.registerState != RegisterState.NOT_SENT) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.registerState != RegisterState.NOT_SENT) {
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

	@Override
	public void sendPacket(Packet<?> packet, PacketSendListener callback) {
		this.handler.send(packet, callback);
	}

	private enum RegisterState {
		NOT_SENT,
		SENT,
		RECEIVED,
		NOT_RECEIVED
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) ((AbstractServerPacketHandlerAccessor) this.handler).getConnection();
	}
}
