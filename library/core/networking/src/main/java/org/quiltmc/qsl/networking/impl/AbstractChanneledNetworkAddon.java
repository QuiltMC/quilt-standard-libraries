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
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;

/**
 * A network addon which is aware of the channels the other side may receive.
 *
 * @param <H> the channel handler type
 */
@ApiStatus.Internal
public abstract class AbstractChanneledNetworkAddon<H> extends AbstractNetworkAddon<H> implements PacketSender<CustomPayload>, CommonPacketHandler {
	protected final ClientConnection connection;
	protected final GlobalReceiverRegistry<H> receiver;
	protected final Set<Identifier> sendableChannels;

	protected int commonVersion = -1;

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
	public <T extends CustomPayload> boolean handle(T originalBuf) {
		this.logger.debug("Handling inbound packet from channel with name \"{}\"", originalBuf.id());

		// Handle reserved packets
		if (NetworkingImpl.REGISTER_CHANNEL.equals(originalBuf.id())) {
			this.receiveRegistration(true, ((ChannelPayload) originalBuf));
			return true;
		}

		if (NetworkingImpl.UNREGISTER_CHANNEL.equals(originalBuf.id())) {
			this.receiveRegistration(false, ((ChannelPayload) originalBuf));
			return true;
		}

		@Nullable H handler = this.getHandler(originalBuf.id());

		if (handler == null) {
			return false;
		}

		try {
			this.receive(handler, originalBuf);
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel with name \"{}\"", originalBuf.id(), ex);
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
		if (channels.isEmpty()) {
			return null;
		}

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

	private void addId(List<Identifier> ids, StringBuilder sb) {
		String literal = sb.toString();

		try {
			ids.add(new Identifier(literal));
		} catch (InvalidIdentifierException ex) {
			this.logger.warn("Received invalid channel identifier \"{}\" from connection {}", literal, this.connection);
		}
	}

	public Set<Identifier> getSendableChannels() {
		return Collections.unmodifiableSet(this.sendableChannels);
	}

	// Common packet handlers

	@Override
	public void onVersionPacket(int negotiatedVersion) {
		assert negotiatedVersion == 1; // We only support version 1 for now

		this.commonVersion = negotiatedVersion;
		this.logger.info("Negotiated common packet version {}", this.commonVersion);
	}

	@Override
	public void onRegisterPacket(RegisterPayload payload) {
		if (payload.version() != this.getNegotiatedVersion()) {
			throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(this.commonVersion, payload.version()));
		}

		final String currentPhase = this.getPhase();

		if (currentPhase == null) {
			// We don't support receiving the register packet during this phase. See getPhase() for supported phases.
			// The normal case where the play channels are sent during configuration is handled in the client/common configuration packet handlers.
			logger.warn("Received common register packet for phase {} in network state: {}", payload.phase(), this.receiver.getState());
			return;
		}

		if (!payload.phase().equals(currentPhase)) {
			// We need to handle receiving the play phase during configuration!
			throw new IllegalStateException("Register packet received for phase (%s) on handler for phase(%s)".formatted(payload.phase(), currentPhase));
		}

		this.register(new ArrayList<>(payload.channels()));
	}

	@Override
	public RegisterPayload createRegisterPayload() {
		return new RegisterPayload(this.getNegotiatedVersion(), this.getPhase(), this.getReceivableChannels());
	}

	@Override
	public int getNegotiatedVersion() {
		if (this.commonVersion == -1) {
			throw new IllegalStateException("Not yet negotiated common packet version");
		}

		return this.commonVersion;
	}

	@Nullable
	private String getPhase() {
		return switch (this.receiver.getState()) {
			case PLAY -> RegisterPayload.PLAY;
			case CONFIGURATION -> RegisterPayload.CONFIGURATION;
			default -> null; // We don't support receiving this packet on any other phase
		};
	}
}
