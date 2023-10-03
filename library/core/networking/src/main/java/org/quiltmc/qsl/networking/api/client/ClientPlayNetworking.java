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

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.client.ClientNetworkingImpl;
import org.quiltmc.qsl.networking.impl.client.ClientPlayNetworkAddon;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;

/**
 * Offers access to play stage client-side networking functionalities.
 * <p>
 * Client-side networking functionalities include receiving client-bound packets,
 * sending server-bound packets, and events related to client-side network handlers.
 * <p>
 * This class should be only used on the physical client and for the logical client.
 *
 * @see ClientLoginNetworking
 * @see ClientConfigurationNetworking
 * @see ServerPlayNetworking
 */
@ClientOnly
public final class ClientPlayNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(Identifier)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ClientPlayNetworking#registerGlobalReceiver(Identifier, ChannelReceiver)
	 * @see ClientPlayNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ClientPlayNetworking#registerReceiver(Identifier, CustomChannelReceiver)
	 */
	public static <T extends CustomPayload> boolean registerGlobalReceiver(Identifier channelName, CustomChannelReceiver<T> channelHandler) {
		return ClientNetworkingImpl.PLAY.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(Identifier)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ClientPlayNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)
	 * @see ClientPlayNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ClientPlayNetworking#registerReceiver(Identifier, ChannelReceiver)
	 * @deprecated use {@link ClientPlayNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)}
	 */
	@Deprecated
	public static boolean registerGlobalReceiver(Identifier channelName, ChannelReceiver channelHandler) {
		return ClientNetworkingImpl.PLAY.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ClientPlayNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)
	 * @see ClientPlayNetworking#unregisterReceiver(Identifier)
	 */
	public static @Nullable CustomChannelReceiver<?> unregisterGlobalReceiver(Identifier channelName) {
		return ClientNetworkingImpl.PLAY.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ClientNetworkingImpl.PLAY.getChannels();
	}

	/**
	 * Registers a handler to a channel.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(Identifier)} to unregister the existing handler.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ClientLoginNetworking#registerGlobalReceiver(Identifier, ClientLoginNetworking.QueryRequestReceiver)}
	 * login query has been received, you should use {@link ClientPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * @param channelName the identifier of the channel
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @throws IllegalStateException if the client is not connected to a server
	 * @see ClientPlayConnectionEvents#INIT
	 */
	public static boolean registerReceiver(Identifier channelName, CustomChannelReceiver<?> channelHandler) {
		final ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();

		if (addon != null) {
			return addon.registerChannel(channelName, channelHandler);
		}

		throw new IllegalStateException("Cannot register receiver while not in game!");
	}

	/**
	 * Registers a handler to a channel.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(Identifier)} to unregister the existing handler.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ClientLoginNetworking#registerGlobalReceiver(Identifier, ClientLoginNetworking.QueryRequestReceiver)}
	 * login query has been received, you should use {@link ClientPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * @param channelName the identifier of the channel
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @throws IllegalStateException if the client is not connected to a server
	 * @see ClientPlayConnectionEvents#INIT
	 * @deprecated use {@link ClientPlayNetworking#registerReceiver(Identifier, CustomChannelReceiver)}
	 */
	@Deprecated
	public static boolean registerReceiver(Identifier channelName, ChannelReceiver channelHandler) {
		final ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();

		if (addon != null) {
			return addon.registerChannel(channelName, channelHandler);
		}

		throw new IllegalStateException("Cannot register receiver while not in game!");
	}

	/**
	 * Removes the handler of a channel.
	 * <p>
	 * The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @throws IllegalStateException if the client is not connected to a server
	 */
	public static @Nullable CustomChannelReceiver<?> unregisterReceiver(Identifier channelName) throws IllegalStateException {
		final ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();

		if (addon != null) {
			return addon.unregisterChannel(channelName);
		}

		throw new IllegalStateException("Cannot unregister receiver while not in game!");
	}

	/**
	 * Gets all the channel names that the client can receive packets on.
	 *
	 * @return all the channel names that the client can receive packets on
	 * @throws IllegalStateException if the client is not connected to a server
	 */
	public static Set<Identifier> getReceived() throws IllegalStateException {
		final ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();

		if (addon != null) {
			return addon.getReceivableChannels();
		}

		throw new IllegalStateException("Cannot get a list of channels the client can receive packets on while not in game!");
	}

	/**
	 * Gets all channel names that the connected server declared the ability to receive a packets on.
	 *
	 * @return all the channel names the connected server declared the ability to receive a packets on
	 * @throws IllegalStateException if the client is not connected to a server
	 */
	public static Set<Identifier> getSendable() throws IllegalStateException {
		final ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();

		if (addon != null) {
			return addon.getSendableChannels();
		}

		throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not in game!");
	}

	/**
	 * Checks if the connected server declared the ability to receive a packet on a specified channel name.
	 *
	 * @param channelName the channel name
	 * @return {@code true} if the connected server has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static boolean canSend(Identifier channelName) throws IllegalArgumentException {
		// You cant send without a client player, so this is fine
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			return ClientNetworkingImpl.getAddon(MinecraftClient.getInstance().getNetworkHandler()).getSendableChannels().contains(channelName);
		}

		return false;
	}

	/**
	 * Creates a packet which may be sent to the connected server.
	 *
	 * @param channelName the channel name
	 * @param buf         the packet byte data which represents the payload of the packet
	 * @return a new packet
	 */
	@Contract(value = "_, _ -> new", pure = true)
	public static Packet<ServerCommonPacketListener> createC2SPacket(@NotNull Identifier channelName, @NotNull PacketByteBuf buf) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(buf, "Buf cannot be null");

		return ClientNetworkingImpl.createC2SPacket(channelName, buf);
	}

	/**
	 * Creates a packet from the payload which may be sent to the connected server.
	 *
	 * @param payload the payload for the packet
	 * @return a new packet
	 */
	@Contract(value = "_ -> new", pure = true)
	public static Packet<ServerCommonPacketListener> createC2SPacket(@NotNull CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");

		return ClientNetworkingImpl.createC2SPacket(payload);
	}

	/**
	 * Gets the packet sender which sends packets to the connected server.
	 *
	 * @return the client's packet sender
	 * @throws IllegalStateException if the client is not connected to a server
	 */
	public static PacketSender<CustomPayload> getSender() throws IllegalStateException {
		// You cant send without a client player, so this is fine
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			return ClientNetworkingImpl.getAddon(MinecraftClient.getInstance().getNetworkHandler());
		}

		throw new IllegalStateException("Cannot get packet sender when not in game!");
	}

	/**
	 * Sends a packet to the connected server.
	 *
	 * @param channelName the channel of the packet
	 * @param buf         the payload of the packet
	 * @throws IllegalStateException if the client is not connected to a server
	 */
	public static void send(Identifier channelName, PacketByteBuf buf) throws IllegalStateException {
		// You cant send without a client player, so this is fine
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			MinecraftClient.getInstance().getNetworkHandler().getConnection().send(createC2SPacket(channelName, buf));
			return;
		}

		throw new IllegalStateException("Cannot send packets when not in game!");
	}

	private ClientPlayNetworking() {
	}

	@ClientOnly
	@FunctionalInterface
	public interface CustomChannelReceiver<T extends CustomPayload> {
		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft client instance.
		 * <p>
		 * An example usage of this is to display an overlay message:
		 * <pre>{@code
		 * ClientPlayNetworking.registerReceiver(new Identifier("mymod", "overlay"), (client, handler, data, responseSender) -&rt; {
		 * 	String message = data.readString(32767);
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	client.execute(() -> {
		 * 		client.inGameHud.setOverlayMessage(message, true);
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param client         the client
		 * @param handler        the network handler that received this packet
		 * @param payload            the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftClient client, ClientPlayNetworkHandler handler, T payload, PacketSender<CustomPayload> responseSender);
	}

	/**
	 * This functional interface should only be used when sending a raw {@link PacketByteBuf} is necessary.
	 * @deprecated use {@link CustomChannelReceiver}
	 */
	@Deprecated
	@ClientOnly
	@FunctionalInterface
	public interface ChannelReceiver extends CustomChannelReceiver<PacketByteBufPayload> {
		@Override
		default void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBufPayload payload, PacketSender<CustomPayload> responseSender) {
			this.receive(client, handler, payload.data(), responseSender);
		}

		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft client instance.
		 * <p>
		 * An example usage of this is to display an overlay message:
		 * <pre>{@code
		 * ClientPlayNetworking.registerReceiver(new Identifier("mymod", "overlay"), (client, handler, data, responseSender) -&rt; {
		 * 	String message = data.readString(32767);
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	client.execute(() -> {
		 * 		client.inGameHud.setOverlayMessage(message, true);
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param client         the client
		 * @param handler        the network handler that received this packet
		 * @param buf            the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender<CustomPayload> responseSender);
	}
}
