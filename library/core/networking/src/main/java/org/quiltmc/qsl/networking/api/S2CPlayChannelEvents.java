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

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Offers access to events related to the indication of a connected client's ability to receive packets in certain channels.
 */
public final class S2CPlayChannelEvents {
	/**
	 * An event for the server play network handler receiving an update indicating the connected client's ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Register> REGISTER = Event.create(Register.class, callbacks -> (handler, sender, server, channels) -> {
		for (Register callback : callbacks) {
			callback.onChannelRegister(handler, sender, server, channels);
		}
	});

	/**
	 * An event for the server play network handler receiving an update indicating the connected client's lack of ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Unregister> UNREGISTER = Event.create(Unregister.class, callbacks -> (handler, sender, server, channels) -> {
		for (Unregister callback : callbacks) {
			callback.onChannelUnregister(handler, sender, server, channels);
		}
	});

	private S2CPlayChannelEvents() {
	}

	/**
	 * @see S2CPlayChannelEvents#REGISTER
	 */
	@FunctionalInterface
	public interface Register extends EventAwareListener {
		void onChannelRegister(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server, List<Identifier> channels);
	}

	/**
	 * @see S2CPlayChannelEvents#UNREGISTER
	 */
	@FunctionalInterface
	public interface Unregister extends EventAwareListener {
		void onChannelUnregister(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server, List<Identifier> channels);
	}
}
