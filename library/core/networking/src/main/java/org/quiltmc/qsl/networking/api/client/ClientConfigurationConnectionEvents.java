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

package org.quiltmc.qsl.networking.api.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.networking.api.PacketSender;

/**
 * Offers access to events related to the connection to a server on a logical client.
 */
@ClientOnly
public final class ClientConfigurationConnectionEvents {
	/**
	 * Event indicating a connection entered the CONFIGURATION state, ready for registering channel handlers.
	 *
	 * @see ClientConfigurationNetworking#registerReceiver(Identifier, ClientConfigurationNetworking.CustomChannelReceiver)
	 */
	public static final Event<Init> INIT = Event.create(Init.class, callbacks -> (handler, client) -> {
		for (Init callback : callbacks) {
			callback.onConfigurationInit(handler, client);
		}
	});

	/**
	 * An event for notification when the client play network handler is ready to send packets to the server.
	 * <p>
	 * At this stage, the network handler is ready to send packets to the server since the client's local state has been set up.
	 */
	public static final Event<Ready> READY = Event.create(Ready.class, callbacks -> (handler, sender, client) -> {
		for (Ready callback : callbacks) {
			callback.onConfigurationReady(handler, sender, client);
		}
	});

	/**
	 * An event for the disconnection of the client play network handler.
	 * <p>
	 * No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = Event.create(Disconnect.class, callbacks -> (handler, client) -> {
		for (Disconnect callback : callbacks) {
			callback.onConfigurationDisconnect(handler, client);
		}
	});

	private ClientConfigurationConnectionEvents() {
	}

	/**
	 * @see #INIT
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Init extends ClientEventAwareListener {
		void onConfigurationInit(ClientConfigurationNetworkHandler handler, MinecraftClient client);
	}

	/**
	 * @see #READY
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Ready extends ClientEventAwareListener {
		void onConfigurationReady(ClientConfigurationNetworkHandler handler, PacketSender<CustomPayload> sender, MinecraftClient client);
	}

	/**
	 * @see #DISCONNECT
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Disconnect extends ClientEventAwareListener {
		void onConfigurationDisconnect(ClientConfigurationNetworkHandler handler, MinecraftClient client);
	}
}
