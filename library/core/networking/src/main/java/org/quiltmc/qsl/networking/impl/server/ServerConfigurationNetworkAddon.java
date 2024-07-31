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

import net.minecraft.network.NetworkPhase;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.PingS2CPacket;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.networking.api.server.S2CConfigurationChannelEvents;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationTaskManager;
import org.quiltmc.qsl.networking.impl.AbstractChanneledNetworkAddon;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;

@ApiStatus.Internal
public final class ServerConfigurationNetworkAddon extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.CustomChannelReceiver<?>> {
	private final ServerConfigurationNetworkHandler handler;
	private final MinecraftServer server;
	private boolean sentInitialRegisterPacket = false;
	public static int PING_ID = 0x0C147; // Somewhat looks like QUILT?

	public ServerConfigurationNetworkAddon(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
		super(ServerNetworkingImpl.CONFIGURATION, ((AbstractServerPacketHandlerAccessor) handler).getConnection(), "ServerConfigurationNetworkAddon for " + handler.getHost().getName());
		this.handler = handler;
		this.server = server;

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkPhase.CONFIGURATION);

		// Register global receivers and attach to session
		this.receiver.startSession(this);
	}

	@Override
	public void lateInit() {
		for (Map.Entry<CustomPayload.Id<?>, ServerConfigurationNetworking.CustomChannelReceiver<?>> entry : this.receiver.getReceivers().entrySet()) {
			this.registerChannel(entry.getKey(), entry.getValue());
		}

		ServerConfigurationConnectionEvents.INIT.invoker().onConfigurationInit(this.handler, this.server);
	}

	public void onConfigureReady() {
		ServerConfigurationConnectionEvents.READY.invoker().onConfigurationReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sendPacket(new PingS2CPacket(PING_ID)); // If we get pong before channels, its a vanilla or non-supported client.
		this.sentInitialRegisterPacket = true;
	}

	@Override
	public <T extends CustomPayload> boolean handle(T payload) {
		boolean handled = super.handle(payload);
		if (handled && payload.getId().equals(NetworkingImpl.REGISTER_CHANNEL)) {
			if (((ServerConfigurationTaskManager) this.handler).getCurrentTask() instanceof SendChannelsTask) {
				ServerConfigurationConnectionEvents.ADD_TASKS.invoker().onAddTasks(this.handler, this.server);
				((ServerConfigurationTaskManager) this.handler).finishTask(SendChannelsTask.TYPE);
			}
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
	protected void invokeRegisterEvent(List<CustomPayload.Id<?>> ids) {
		S2CConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<CustomPayload.Id<?>> ids) {
		S2CConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(CustomPayload.Id<?> channelName) {
		if (this.sentInitialRegisterPacket) {
			final ChannelPayload payload = this.createRegistrationPacket(List.of(channelName), true);

			if (payload != null) {
				this.sendPacket(new CustomPayloadS2CPacket(payload));
			}
		}
	}

	@Override
	protected void handleUnregistration(CustomPayload.Id<?> channelName) {
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
	protected boolean isReservedChannel(CustomPayload.Id<?> channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) ((AbstractServerPacketHandlerAccessor) this.handler).getConnection();
	}
}
