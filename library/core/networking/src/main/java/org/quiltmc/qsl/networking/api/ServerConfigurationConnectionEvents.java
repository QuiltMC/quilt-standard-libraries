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

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is in game.
 */
// TODO: Double check these events with fabric. Also make sure that the add tasks event is invoked or fold it into the ready event.
public final class ServerConfigurationConnectionEvents {
	/**
	 * Event indicating a connection entered the CONFIGURATION state, ready for registering channel handlers.
	 *
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketHandler, Identifier, ServerConfigurationNetworking.CustomChannelReceiver)
	 */
	public static final Event<Init> INIT = Event.create(Init.class, callbacks -> (handler, server) -> {
		for (Init callback : callbacks) {
			callback.onConfigurationInit(handler, server);
		}
	});

	/**
	 * An event for notification when the server configuration network handler is ready to send packets to the client.
	 * <p>
	 * At this stage, the network handler is ready to send packets to the client.
	 */
	public static final Event<Join> READY = Event.create(Join.class, callbacks -> (handler, sender, server) -> {
		for (Join callback : callbacks) {
			callback.onConfigurationReady(handler, sender, server);
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

	/**
	 * An event for adding tasks to a server configuration network handler.
	 * Called once per connection when the optional tasks are also added.
	 */
	public static final Event<AddTasks> ADD_TASKS = Event.create(AddTasks.class, callbacks -> (handler, server) -> {
		for (AddTasks callback : callbacks) {
			callback.onAddTasks(handler, server);
		}
	});

	private ServerConfigurationConnectionEvents() {
	}

	/**
	 * @see #INIT
	 */
	@FunctionalInterface
	public interface Init extends EventAwareListener {
		void onConfigurationInit(ServerConfigurationPacketHandler handler, MinecraftServer server);
	}

	/**
	 * @see #READY
	 */
	@FunctionalInterface
	public interface Join extends EventAwareListener {
		void onConfigurationReady(ServerConfigurationPacketHandler handler, PacketSender<CustomPayload> sender, MinecraftServer server);
	}

	/**
	 * @see #DISCONNECT
	 */
	@FunctionalInterface
	public interface Disconnect extends EventAwareListener {
		void onConfigurationDisconnect(ServerConfigurationPacketHandler handler, MinecraftServer server);
	}

	/**
	 * @see #ADD_TASKS
	 */
	@FunctionalInterface
	public interface AddTasks extends EventAwareListener {
		void onAddTasks(ServerConfigurationPacketHandler handler, MinecraftServer server);
	}
}
