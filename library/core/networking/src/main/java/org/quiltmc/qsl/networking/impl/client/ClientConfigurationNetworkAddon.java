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

package org.quiltmc.qsl.networking.impl.client;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.client.C2SConfigurationChannelEvents;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.ClientConfigurationNetworkHandlerAccessor;

@ApiStatus.Internal
@ClientOnly
public final class ClientConfigurationNetworkAddon extends AbstractChanneledNetworkAddon<ClientConfigurationNetworking.CustomChannelReceiver<?>> {
	private final ClientConfigurationNetworkHandler handler;
	private final MinecraftClient client;
	private boolean sentInitialRegisterPacket;

	public ClientConfigurationNetworkAddon(ClientConfigurationNetworkHandler handler, MinecraftClient client) {
		super(ClientNetworkingImpl.CONFIGURATION,
				((ClientConfigurationNetworkHandlerAccessor) handler).getConnection(),
				"ClientConfigurationNetworkAddon for " + ((ClientConfigurationNetworkHandlerAccessor) handler).getProfile().getName());
		this.handler = handler;
		this.client = client;

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkState.CONFIGURATION);

		// Register global receivers and attach to session
		this.receiver.startSession(this);
	}

	@Override
	public void lateInit() {
		for (Map.Entry<Identifier, ClientConfigurationNetworking.CustomChannelReceiver<?>> entry : this.receiver.getReceivers().entrySet()) {
			this.registerChannel(entry.getKey(), entry.getValue());
		}

		ClientConfigurationConnectionEvents.INIT.invoker().onConfigurationInit(this.handler, this.client);
		ClientConfigurationConnectionEvents.READY.invoker().onConfigurationReady(this.handler, this, this.client);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends CustomPayload> void receive(ClientConfigurationNetworking.CustomChannelReceiver<?> handler, T buf) {
		((ClientConfigurationNetworking.CustomChannelReceiver<T>) handler).receive(this.client, this.handler, buf, this);
	}

	@Override
	public Packet<?> createPacket(CustomPayload payload) {
		return ClientNetworkingImpl.createC2SPacket(payload);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		MinecraftClient.getInstance().execute(task);
	}

	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return ClientConfigurationNetworking.createC2SPacket(channelName, buf);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.client, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.client, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadC2SPacket(payload));
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadC2SPacket(payload));
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(this.handler, this.client);
		this.receiver.endSession(this);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}
}
