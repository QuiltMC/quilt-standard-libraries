/*
 * Copyright 2022-2023 QuiltMC
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
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Offers access to events related to the connection to a server on the client while the server is processing the client's login request.
 */
@ClientOnly
public final class ClientLoginConnectionEvents {
	/**
	 * Event indicating a connection entered the LOGIN state, ready for registering query request handlers.
	 * This event may be used by mods to prepare their client side state.
	 * This event does not guarantee that a login attempt will be successful.
	 *
	 * @see ClientLoginNetworking#registerReceiver(Identifier, ClientLoginNetworking.QueryRequestReceiver)
	 */
	public static final Event<Init> INIT = Event.create(Init.class, callbacks -> (handler, client) -> {
		for (Init callback : callbacks) {
			callback.onLoginStart(handler, client);
		}
	});

	/**
	 * An event for when the client has started receiving login queries.
	 * A client can only start receiving login queries when a server has sent the first login query.
	 * Vanilla servers will typically never make the client enter this login phase, but it is not a guarantee that the
	 * connected server is a vanilla server since a modded server or proxy may have no login queries to send to the client
	 * and therefore bypass the login query phase.
	 * If this event is fired then it is a sign that a server is not a vanilla server or the server is behind a proxy which
	 * is capable of handling login queries.
	 * <p>
	 * This event may be used to {@link ClientLoginNetworking.QueryRequestReceiver register login query handlers}
	 * which may be used to send a response to a server.
	 * <p>
	 * No packets should be sent when this event is invoked.
	 */
	public static final Event<QueryStart> QUERY_START = Event.create(QueryStart.class, callbacks -> (handler, client) -> {
		for (QueryStart callback : callbacks) {
			callback.onLoginQueryStart(handler, client);
		}
	});

	/**
	 * An event for when the client's login process has ended due to disconnection.
	 * <p>
	 * No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = Event.create(Disconnect.class, callbacks -> (handler, client) -> {
		for (Disconnect callback : callbacks) {
			callback.onLoginDisconnect(handler, client);
		}
	});

	private ClientLoginConnectionEvents() {
	}

	/**
	 * @see ClientLoginConnectionEvents#INIT
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Init extends ClientEventAwareListener {
		void onLoginStart(ClientLoginNetworkHandler handler, MinecraftClient client);
	}

	/**
	 * @see ClientLoginConnectionEvents#QUERY_START
	 */
	@ClientOnly
	@FunctionalInterface
	public interface QueryStart extends ClientEventAwareListener {
		void onLoginQueryStart(ClientLoginNetworkHandler handler, MinecraftClient client);
	}

	/**
	 * @see ClientLoginConnectionEvents#DISCONNECT
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Disconnect extends ClientEventAwareListener {
		void onLoginDisconnect(ClientLoginNetworkHandler handler, MinecraftClient client);
	}
}
