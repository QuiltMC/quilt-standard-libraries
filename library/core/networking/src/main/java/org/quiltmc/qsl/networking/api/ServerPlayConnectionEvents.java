/*
 * Copyright 2016, 2017 QuiltMC
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

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is in game.
 */
public final class ServerPlayConnectionEvents {
	/**
	 * Event indicating a connection entered the PLAY state, ready for registering channel handlers.
	 *
	 * @see ServerPlayNetworking#registerReceiver(ServerPlayNetworkHandler, Identifier, ServerPlayNetworking.PlayChannelHandler)
	 */
	public static final ArrayEvent<Init> INIT = ArrayEvent.create(Init.class, callbacks -> (handler, server) -> {
		for (Init callback : callbacks) {
			callback.onPlayInit(handler, server);
		}
	});

	/**
	 * An event for notification when the server play network handler is ready to send packets to the client.
	 *
	 * <p>At this stage, the network handler is ready to send packets to the client.
	 */
	public static final ArrayEvent<Join> JOIN = ArrayEvent.create(Join.class, callbacks -> (handler, sender, server) -> {
		for (Join callback : callbacks) {
			callback.onPlayReady(handler, sender, server);
		}
	});

	/**
	 * An event for the disconnection of the server play network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final ArrayEvent<Disconnect> DISCONNECT = ArrayEvent.create(Disconnect.class, callbacks -> (handler, server) -> {
		for (Disconnect callback : callbacks) {
			callback.onPlayDisconnect(handler, server);
		}
	});

	private ServerPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server);
	}
}
