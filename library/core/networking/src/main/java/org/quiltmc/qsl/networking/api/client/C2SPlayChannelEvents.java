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

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.networking.api.PacketSender;

/**
 * Offers access to events related to the indication of a connected server's ability to receive packets in certain channels.
 */
@ClientOnly
public final class C2SPlayChannelEvents {
	/**
	 * An event for the client play network handler receiving an update indicating the connected server's ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Register> REGISTER = Event.create(Register.class, callbacks -> (handler, sender, client, channels) -> {
		for (Register callback : callbacks) {
			callback.onChannelRegister(handler, sender, client, channels);
		}
	});

	/**
	 * An event for the client play network handler receiving an update indicating the connected server's lack of ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Unregister> UNREGISTER = Event.create(Unregister.class, callbacks -> (handler, sender, client, channels) -> {
		for (Unregister callback : callbacks) {
			callback.onChannelUnregister(handler, sender, client, channels);
		}
	});

	private C2SPlayChannelEvents() {
	}

	/**
	 * @see C2SPlayChannelEvents#REGISTER
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Register extends ClientEventAwareListener {
		void onChannelRegister(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, List<Identifier> channels);
	}

	/**
	 * @see C2SPlayChannelEvents#UNREGISTER
	 */
	@ClientOnly
	@FunctionalInterface
	public interface Unregister extends ClientEventAwareListener {
		void onChannelUnregister(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, List<Identifier> channels);
	}
}
