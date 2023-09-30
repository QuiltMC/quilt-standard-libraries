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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;

/**
 * Offers access to play stage server-side networking functionalities.
 * <p>
 * Server-side networking functionalities include receiving server-bound packets, sending client-bound packets,
 * and events related to server-side network handlers.
 * <p>
 * This class should be only used for the logical server.
 *
 * @see ServerLoginNetworking
 * @see ClientPlayNetworking
 */
public final class ServerPlayNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ServerPlayNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerPlayNetworking#registerReceiver(ServerPlayNetworkHandler, Identifier, ChannelReceiver)
	 */
	public static boolean registerGlobalReceiver(Identifier channelName, ChannelReceiver channelHandler) {
		return ServerNetworkingImpl.PLAY.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerPlayNetworking#registerGlobalReceiver(Identifier, ChannelReceiver)
	 * @see ServerPlayNetworking#unregisterReceiver(ServerPlayNetworkHandler, Identifier)
	 */
	@Nullable
	public static ServerPlayNetworking.ChannelReceiver unregisterGlobalReceiver(Identifier channelName) {
		return ServerNetworkingImpl.PLAY.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.PLAY.getChannels();
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerPlayNetworking#registerGlobalReceiver(Identifier, ChannelReceiver)} since
	 * the channel handler will only be applied to the player represented by the {@link ServerPlayNetworkHandler}.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.QueryResponseReceiver)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 * <p>
	 * If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param networkHandler the handler
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel name, otherwise {@code true}
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static boolean registerReceiver(ServerPlayNetworkHandler networkHandler, Identifier channelName, ChannelReceiver channelHandler) {
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
	public static ServerPlayNetworking.ChannelReceiver unregisterReceiver(ServerPlayNetworkHandler networkHandler, Identifier channelName) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).unregisterChannel(channelName);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param player the player
	 * @return all the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerPlayerEntity player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getReceived(player.networkHandler);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return all the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerPlayNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getReceivableChannels();
	}

	/**
	 * Gets all channel names that the connected client declared the ability to receive a packets on.
	 *
	 * @param player the player
	 * @return all the channel names the connected client declared the ability to receive a packets on
	 */
	public static Set<Identifier> getSendable(ServerPlayerEntity player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getSendable(player.networkHandler);
	}

	/**
	 * Gets all channel names that a the connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static Set<Identifier> getSendable(ServerPlayNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels();
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param player      the player
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static boolean canSend(ServerPlayerEntity player, Identifier channelName) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return canSend(player.networkHandler, channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler     the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static boolean canSend(ServerPlayNetworkHandler handler, Identifier channelName) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(channelName);
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param channelName the channel name
	 * @param buf         the packet byte buf which represents the payload of the packet
	 * @return a new packet
	 */
	@Contract(value = "_, _ -> new", pure = true)
	public static Packet<ClientPlayPacketListener> createS2CPacket(@NotNull Identifier channelName, @NotNull PacketByteBuf buf) {
		Objects.requireNonNull(channelName, "Channel cannot be null");
		Objects.requireNonNull(buf, "Buf cannot be null");

		return ServerNetworkingImpl.createPlayC2SPacket(channelName, buf);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param player the player
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerPlayerEntity player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getSender(player.networkHandler);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the player/client
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerPlayNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler);
	}

	/**
	 * Sends a packet to a player.
	 *
	 * @param player      the player to send the packet to
	 * @param channelName the channel of the packet
	 * @param buf         the payload of the packet
	 */
	public static void send(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf) {
		Objects.requireNonNull(player, "Server player entity cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(buf, "Packet byte buf cannot be null");

		player.networkHandler.sendPacket(createS2CPacket(channelName, buf));
	}

	/**
	 * Sends a packet to a collection of players.
	 *
	 * @param players     the players to send the packet to
	 * @param channelName the channel of the packet
	 * @param buf         the payload of the packet
	 */
	public static void send(Collection<ServerPlayerEntity> players, Identifier channelName, PacketByteBuf buf) {
		Objects.requireNonNull(players, "Players collection cannot be null");

		players.forEach(player -> send(player, channelName, buf));
	}

	// Helper methods

	// TODO: Possible future CHASM extension method.

	/**
	 * Returns the <i>Minecraft</i> Server of a server play network handler.
	 *
	 * @param handler the server play network handler
	 */
	public static MinecraftServer getServer(ServerPlayNetworkHandler handler) {
		Objects.requireNonNull(handler, "Network handler cannot be null");

		return handler.player.server;
	}

	private ServerPlayNetworking() {
	}

	@FunctionalInterface
	public interface ChannelReceiver {
		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft server instance.
		 * <p>
		 * An example usage of this is to create an explosion where the player is looking:
		 * <pre>{@code
		 * ServerPlayNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, player, handler, buf, responseSender) -> {
		 * 	boolean fire = buf.readBoolean();
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	server.execute(() -> {
		 * 		ModPacketHandler.createExplosion(player, fire);
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param server         the server
		 * @param player         the player
		 * @param handler        the network handler that received this packet, representing the player/client who sent the packet
		 * @param buf            the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender);
	}
}
