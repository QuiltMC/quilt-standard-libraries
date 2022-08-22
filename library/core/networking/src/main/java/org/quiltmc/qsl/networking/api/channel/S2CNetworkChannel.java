package org.quiltmc.qsl.networking.api.channel;

import java.util.Collection;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

/**
 * A network channel that can send message from server to client.
 *
 * @param <T> the type of messages this channel can send
 */
public interface S2CNetworkChannel<T> extends NetworkChannel<T> {
	/**
	 * Send a message to a specific player.
	 *
	 * @param player  the player to send the message to
	 * @param message the message to send
	 */
	default void sendToPlayer(ServerPlayerEntity player, T message) {
		ServerPlayNetworking.send(player, this.id(), this.codec().createBuffer(message));
	}

	/**
	 * Send a message to many players.
	 *
	 * @param players the players to send the message to
	 * @param message the message to send
	 */
	default void sendTo(Collection<ServerPlayerEntity> players, T message) {
		ServerPlayNetworking.send(players, this.id(), this.codec().createBuffer(message));
	}

	/**
	 * Send a message to all players that are tracking the specified {@link BlockPos}.
	 *
	 * @param message the message to send
	 * @param world   the world to send the message in
	 * @param pos     the {@link BlockPos} that a player <b>must</b> track to receive the message
	 * @see PlayerLookup#tracking(ServerWorld, BlockPos)
	 */
	default void sendToTrackers(T message, ServerWorld world, BlockPos pos) {
		this.sendTo(PlayerLookup.tracking(world, pos), message);
	}

	/**
	 * Send a message to all players that are tracking the specified {@link BlockEntity}.
	 *
	 * @param message     the message to send
	 * @param blockEntity the {@link BlockEntity} that a player <b>must</b> track to receive the message
	 * @see PlayerLookup#tracking(BlockEntity)
	 */
	default void sendToTrackers(T message, BlockEntity blockEntity) {
		this.sendTo(PlayerLookup.tracking(blockEntity), message);
	}

	/**
	 * Send a message to all players that are tracking the specified {@link Entity}.
	 *
	 * @param message the message to send
	 * @param entity  the {@link Entity} that a player <b>must</b> track to receive the message
	 * @see PlayerLookup#tracking(Entity)
	 */
	default void sendToTrackers(T message, Entity entity) {
		this.sendTo(PlayerLookup.tracking(entity), message);
	}

	/**
	 * Send a message to all players that are tracking the chunk at the specified {@link ChunkPos}.
	 *
	 * @param message the message to send
	 * @param pos     the {@link ChunkPos} of the {@link net.minecraft.world.chunk.Chunk} that a player <b>must</b>
	 *                track to receive the message
	 * @see PlayerLookup#tracking(ServerWorld, ChunkPos)
	 */
	default void sendToTrackers(T message, ServerWorld world, ChunkPos pos) {
		this.sendTo(PlayerLookup.tracking(world, pos), message);
	}

	/**
	 * Send a message to all players that are in the specified {@link ServerWorld}.
	 *
	 * @param message the message to send
	 * @param world   the {@link ServerWorld} that a player <b>must</b> be in to receive the message
	 * @see PlayerLookup#world(ServerWorld)
	 */
	default void sendToWorld(T message, ServerWorld world) {
		this.sendTo(PlayerLookup.world(world), message);
	}

	/**
	 * Send a message to all connected players.
	 *
	 * @param message the message to send
	 * @param server  the server that the message is sent from
	 * @see PlayerLookup#all(MinecraftServer)
	 */
	default void sendToAll(T message, MinecraftServer server) {
		this.sendTo(PlayerLookup.all(server), message);
	}

	/**
	 * Creates a {@link ClientPlayNetworking.ChannelReceiver} that uses this {@link NetworkChannel}'s
	 * {@linkplain org.quiltmc.qsl.networking.api.codec.NetworkCodec codec} to decode messages and can handle them.
	 *
	 * @return a {@link ClientPlayNetworking.ChannelReceiver} that can receive messages from this
	 * {@link NetworkChannel}
	 */
	@Environment(EnvType.CLIENT)
	ClientPlayNetworking.ChannelReceiver createClientReceiver();

	/**
	 * Interface used to handle messages on the client.
	 */
	@FunctionalInterface
	interface Handler {
		/**
		 * Handle a message on the client.
		 *
		 * @param client         the client
		 * @param handler        the {@link ClientPlayNetworkHandler} that received the message
		 * @param responseSender the {@link PacketSender} to use to send responses
		 * @apiNote This method is called on the main thread.
		 * @implNote Always make sure implementations of this method are marked as client-only using
		 * {@link Environment} with a value of {@link EnvType#CLIENT}.
		 */
		@Environment(EnvType.CLIENT)
		void clientHandle(
				MinecraftClient client,
				ClientPlayNetworkHandler handler,
				PacketSender responseSender
		);
	}
}
