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

package org.quiltmc.qsl.networking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;

/**
 * A network addon which is aware of the channels the other side may receive.
 *
 * @param <H> the channel handler type
 */
@ApiStatus.Internal
public abstract class AbstractChanneledNetworkAddon<H> extends AbstractNetworkAddon<H> implements PacketSender<CustomPayload> {
	protected final ClientConnection connection;
	protected final GlobalReceiverRegistry<H> receiver;
	protected final Set<Identifier> sendableChannels;

	protected AbstractChanneledNetworkAddon(GlobalReceiverRegistry<H> receiver, ClientConnection connection, String description) {
		super(receiver, description);
		this.connection = connection;
		this.receiver = receiver;
		this.sendableChannels = Collections.synchronizedSet(new HashSet<>());
	}

	public abstract void lateInit();

	protected void registerPendingChannels(ChannelInfoHolder holder, NetworkState state) {
		final Collection<Identifier> pending = holder.getPendingChannelsNames(state);

		if (!pending.isEmpty()) {
			this.register(new ArrayList<>(pending));
			pending.clear();
		}
	}

	// always supposed to handle async!
	public <T extends CustomPayload> boolean handle(T payload) {
		this.logger.debug("Handling inbound packet from channel with name \"{}\"", payload.id());

		// Handle reserved packets
		if (NetworkingImpl.REGISTER_CHANNEL.equals(payload.id())) {
			this.receiveRegistration(true, ((ChannelPayload) payload));
			return true;
		}

		if (NetworkingImpl.UNREGISTER_CHANNEL.equals(payload.id())) {
			this.receiveRegistration(false, ((ChannelPayload) payload));
			return true;
		}

		@Nullable H handler = this.getHandler(payload.id());

		if (handler == null) {
			return false;
		}

		try {
			this.receive(handler, payload);
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel with name \"{}\"", payload.id(), ex);
			throw ex;
		}

		return true;
	}

	protected abstract <T extends CustomPayload> void receive(H handler, T buf);

	protected void sendInitialChannelRegistrationPacket() {
		final ChannelPayload payload = this.createRegistrationPacket(List.copyOf(this.getReceivableChannels()), true);

		if (payload != null) {
			this.sendPacket(this.createPacket(payload));
		}
	}

	@Nullable
	protected ChannelPayload createRegistrationPacket(List<Identifier> channels, boolean register) {
		return register ? new ChannelPayload.RegisterChannelPayload(channels) : new ChannelPayload.UnregisterChannelPayload(channels);
	}

	// wrap in try with res (payload)
	protected void receiveRegistration(boolean register, ChannelPayload payload) {
		if (register) {
			this.register(payload.channels());
		} else {
			this.unregister(payload.channels());
		}
	}

	void register(List<Identifier> ids) {
		this.sendableChannels.addAll(ids);
		this.schedule(() -> this.invokeRegisterEvent(ids));
	}

	void unregister(List<Identifier> ids) {
		this.sendableChannels.removeAll(ids);
		this.schedule(() -> this.invokeUnregisterEvent(ids));
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		Objects.requireNonNull(packet, "Packet cannot be null");

		this.connection.send(packet);
	}

	@Override
	public void sendPacket(Packet<?> packet, PacketSendListener callback) {
		Objects.requireNonNull(packet, "Packet cannot be null");

		this.connection.send(packet, callback);
	}

	/**
	 * Schedules a task to run on the main thread.
	 */
	protected abstract void schedule(Runnable task);

	protected abstract void invokeRegisterEvent(List<Identifier> ids);

	protected abstract void invokeUnregisterEvent(List<Identifier> ids);

	public Set<Identifier> getSendableChannels() {
		return Collections.unmodifiableSet(this.sendableChannels);
	}
}
