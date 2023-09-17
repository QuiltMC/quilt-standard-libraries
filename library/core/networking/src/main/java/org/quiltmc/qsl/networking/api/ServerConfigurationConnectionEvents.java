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

package org.quiltmc.qsl.networking.api;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is in game.
 */
public final class ServerConfigurationConnectionEvents {
	/**
	 * Event indicating a connection entered the CONFIGURATION state, ready for registering channel handlers.
	 *
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketHandler, Identifier, ServerConfigurationNetworking.ChannelReceiver)
	 */
	public static final Event<Configure> BEFORE_CONFIGURE = Event.create(Configure.class, callbacks -> (handler, server) -> {
		for (Configure callback : callbacks) {
			callback.onConfiguration(handler, server);
		}
	});

	/**
	 * An event fired during the CONFIGURATION state.
	 * <p>
	 * At this stage, the network handler is ready to send packets to the client.
	 */
	public static final Event<Configure> CONFIGURE = Event.create(Configure.class, callbacks -> (handler, server) -> {
		for (Configure callback : callbacks) {
			callback.onConfiguration(handler, server);
		}
	});

	/**
	 * An event for the disconnection of the server configuration network handler.
	 * <p>
	 * No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = Event.create(Disconnect.class, callbacks -> (handler, server) -> {
		for (Disconnect callback : callbacks) {
			callback.onConfigurationDisconnect(handler, server);
		}
	});

	private ServerConfigurationConnectionEvents() {
	}

	/**
	 * @see #BEFORE_CONFIGURE
	 * @see #CONFIGURE
	 */
	@FunctionalInterface
	public interface Configure extends EventAwareListener {
		void onConfiguration(ServerConfigurationPacketHandler handler, MinecraftServer server);
	}

	/**
	 * @see #DISCONNECT
	 */
	@FunctionalInterface
	public interface Disconnect extends EventAwareListener {
		void onConfigurationDisconnect(ServerConfigurationPacketHandler handler, MinecraftServer server);
	}
}
