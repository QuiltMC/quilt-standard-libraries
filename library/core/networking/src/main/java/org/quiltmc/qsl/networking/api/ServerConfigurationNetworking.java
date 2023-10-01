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

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;

/**
 * Offers access to configuration stage server-side networking functionalities.
 * <p>
 * Server-side networking functionalities include receiving server-bound packets, sending client-bound packets,
 * and events related to server-side network handlers.
 * <p>
 * This class should be only used for the logical server.
 *
 * @see ServerLoginNetworking
 * @see ServerPlayNetworking
 * @see ClientConfigurationNetworking
 */
public final class ServerConfigurationNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationPacketHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketHandler, Identifier, CustomChannelReceiver)
	 */
	public static <T extends CustomPayload> boolean registerGlobalReceiver(Identifier channelName, CustomChannelReceiver<T> channelHandler) {
		return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationPacketHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketHandler, Identifier, ChannelReceiver)
	 * @deprecated use {@link ServerConfigurationNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)}
	 */
	@Deprecated
	public static boolean registerGlobalReceiver(Identifier channelName, ChannelReceiver channelHandler) {
		return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)
	 * @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationPacketHandler, Identifier)
	 */
	@Nullable
	public static ServerConfigurationNetworking.CustomChannelReceiver<?> unregisterGlobalReceiver(Identifier channelName) {
		return ServerNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.CONFIGURATION.getChannels();
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(Identifier, CustomChannelReceiver)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationPacketHandler}.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.QueryResponseReceiver)}
	 * login response has been received, you should use {@link ServerConfigurationConnectionEvents#INIT} to register the channel handler.
	 * <p>
	 * If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationPacketHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param networkHandler the handler
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel name, otherwise {@code true}
	 * @see ServerConfigurationConnectionEvents#INIT
	 */
	public static <T extends CustomPayload> boolean registerReceiver(ServerConfigurationPacketHandler networkHandler, Identifier channelName, CustomChannelReceiver<T> channelHandler) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).registerChannel(channelName, channelHandler);
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(Identifier, ChannelReceiver)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationPacketHandler}.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.QueryResponseReceiver)}
	 * login response has been received, you should use {@link ServerConfigurationConnectionEvents#INIT} to register the channel handler.
	 * <p>
	 * If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationPacketHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param networkHandler the handler
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel name, otherwise {@code true}
	 * @see ServerConfigurationConnectionEvents#INIT
	 * @deprecated use {@link ServerConfigurationNetworking#registerReceiver(ServerConfigurationPacketHandler, Identifier, CustomChannelReceiver)}
	 */
	@Deprecated
	public static boolean registerReceiver(ServerConfigurationPacketHandler networkHandler, Identifier channelName, ChannelReceiver channelHandler) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).registerChannel(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * <p>
	 * The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 */
	@Nullable
	public static ServerConfigurationNetworking.CustomChannelReceiver<?> unregisterReceiver(ServerConfigurationPacketHandler networkHandler, Identifier channelName) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).unregisterChannel(channelName);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return all the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerConfigurationPacketHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getReceivableChannels();
	}

	/**
	 * Gets all channel names that the connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static Set<Identifier> getSendable(ServerConfigurationPacketHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels();
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler     the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static boolean canSend(ServerConfigurationPacketHandler handler, Identifier channelName) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(channelName);
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param channelName the channel name
	 * @param buf         the packet byte data which represents the payload of the packet
	 * @return a new packet
	 */
	@Contract(value = "_, _ -> new", pure = true)
	public static Packet<ClientCommonPacketListener> createS2CPacket(@NotNull Identifier channelName, @NotNull PacketByteBuf buf) {
		Objects.requireNonNull(channelName, "Channel cannot be null");
		Objects.requireNonNull(buf, "Buf cannot be null");

		return ServerNetworkingImpl.createS2CPacket(channelName, buf);
	}

	/**
	 * Creates a packet from a payload which may be sent to a connected client.
	 *
	 * @param payload the payload of the packet
	 * @return a new packet
	 */
	@Contract(value = "_ -> new", pure = true)
	public static Packet<ClientCommonPacketListener> createS2CPacket(@NotNull CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");

		return ServerNetworkingImpl.createS2CPacket(payload);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the client
	 * @return the packet sender
	 */
	public static PacketSender<CustomPayload> getSender(ServerConfigurationPacketHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler);
	}

	/**
	 * Sends a packet to a client.
	 *
	 * @param networkHandler the handler to send the packet to
	 * @param channelName    the channel of the packet
	 * @param buf            the payload of the packet
	 */
	public static void send(ServerConfigurationPacketHandler networkHandler, Identifier channelName, PacketByteBuf buf) {
		Objects.requireNonNull(networkHandler, "Server configuration handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(buf, "Packet byte data cannot be null");

		networkHandler.send(createS2CPacket(channelName, buf));
	}

	// Helper methods

	// TODO: Possible future CHASM extension method.

	/**
	 * Returns the <i>Minecraft</i> Server of a server configuration packet handler.
	 *
	 * @param handler the server configuration packet handler
	 */
	public static MinecraftServer getServer(ServerConfigurationPacketHandler handler) {
		Objects.requireNonNull(handler, "Network handler cannot be null");

		return ((AbstractServerPacketHandlerAccessor) handler).getServer();
	}

	private ServerConfigurationNetworking() {
	}

	@FunctionalInterface
	public interface CustomChannelReceiver<T extends CustomPayload> {
		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft server instance.
		 * <pre>{@code
		 * ServerConfigurationNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, handler, data, responseSender) -> {
		 * 	boolean fire = data.readBoolean();
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	server.execute(() -> {
		 *
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param server         the server
		 * @param handler        the network handler that received this packet, representing the client who sent the packet
		 * @param payload        the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerConfigurationPacketHandler handler, T payload, PacketSender<CustomPayload> responseSender);
	}

	/**
	 * This functional interface should only be used when sending a raw {@link PacketByteBuf} is necessary.
	 * <p>
	 * @deprecated use {@link CustomChannelReceiver}
	 */
	@Deprecated
	@FunctionalInterface
	public interface ChannelReceiver extends CustomChannelReceiver<PacketByteBufPayload> {
		default void receive(MinecraftServer server, ServerConfigurationPacketHandler handler, PacketByteBufPayload payload, PacketSender<CustomPayload> responseSender) {
			this.receive(server, handler, payload.data(), responseSender);
		}

		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft server instance.
		 * <pre>{@code
		 * ServerConfigurationNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, handler, data, responseSender) -> {
		 * 	boolean fire = data.readBoolean();
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	server.execute(() -> {
		 *
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param server         the server
		 * @param handler        the network handler that received this packet, representing the client who sent the packet
		 * @param buf            the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerConfigurationPacketHandler handler, PacketByteBuf buf, PacketSender<CustomPayload> responseSender);
	}
}
