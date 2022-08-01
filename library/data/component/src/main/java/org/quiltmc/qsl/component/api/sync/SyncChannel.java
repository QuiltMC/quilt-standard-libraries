/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.api.sync;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.Syncable;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.client.sync.ClientResolution;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

// ClientResolution.* all reference client-only classes,
// so we need to just make sure we use lambdas so no attempt at class loading non-existent classes
@SuppressWarnings("Convert2MethodRef")
public class SyncChannel<P extends ComponentProvider, U> {
	// BlockEntity
	public static final SyncChannel<BlockEntity, BlockPos> BLOCK_ENTITY = new SyncChannel<>(
			PacketIds.BLOCK_ENTITY_SYNC,
			NetworkCodec.BLOCK_POS,
			BlockEntity::getPos,
			blockPos -> ClientResolution.blockEntity(blockPos),
			(serverPlayer, blockPos) -> serverPlayer.getWorld().getBlockEntity(blockPos),
			PlayerLookup::tracking
	);

	// Entity
	public static final SyncChannel<Entity, Integer> ENTITY = new SyncChannel<>(
			PacketIds.ENTITY_SYNC,
			NetworkCodec.VAR_INT,
			Entity::getId,
			id -> ClientResolution.entity(id),
			(serverPlayer, id) -> serverPlayer.getWorld().getEntityById(id),
			PlayerLookup::tracking
	);

	// Chunk
	public static final SyncChannel<Chunk, ChunkPos> CHUNK = new SyncChannel<>(
			PacketIds.CHUNK_SYNC,
			NetworkCodec.CHUNK_POS,
			Chunk::getPos,
			chunkPos -> ClientResolution.chunk(chunkPos),
			(serverPlayer, chunkPos) -> serverPlayer.getWorld().getChunk(chunkPos.x, chunkPos.z),
			chunk -> PlayerLookup.tracking(((ServerWorld) ((WorldChunk) chunk).getWorld()), chunk.getPos()) // only called server side so the cast is safe
	); // **Careful**: This only works with WorldChunks not other chunk types!

	// World
	public static final SyncChannel<World, Unit> WORLD = new SyncChannel<>(
			PacketIds.WORLD_SYNC,
			NetworkCodec.EMPTY,
			world -> Unit.INSTANCE,
			unit -> ClientResolution.world(),
			(serverPlayer, unit) -> serverPlayer.getWorld(),
			world -> PlayerLookup.world((ServerWorld) world)// only called server side so the cast is safe
	);

	// Level
	public static final SyncChannel<ComponentProvider, Unit> LEVEL = new SyncChannel<>(
			PacketIds.LEVEL_SYNC,
			NetworkCodec.EMPTY,
			provider -> Unit.INSTANCE,
			unit -> ClientResolution.level(),
			(serverPlayer, unit) -> serverPlayer.getServer(),
			provider -> PlayerLookup.all((MinecraftServer) provider) // only called server side so the cast is safe
	);

	protected final Identifier channelId;
	protected final NetworkCodec<U> codec;
	protected final Function<P, U> identifyingDataTransformer;
	protected final Function<U, P> clientLocator;
	protected final BiFunction<ServerPlayerEntity, U, P> serverLocator;
	protected final Function<? super P, Collection<ServerPlayerEntity>> playerProvider;
	@Environment(EnvType.CLIENT)
	protected final Queue<U> requestQueue = new ArrayDeque<>();
	protected final NetworkCodec<Queue<U>> queueCodec;

	public SyncChannel(Identifier channelId, NetworkCodec<U> codec,
					Function<P, U> identifyingDataTransformer,
					Function<U, P> clientLocator, BiFunction<ServerPlayerEntity, U, P> serverLocator,
					Function<? super P, Collection<ServerPlayerEntity>> playerProvider) {
		this.channelId = channelId;
		this.codec = codec;
		this.identifyingDataTransformer = identifyingDataTransformer;
		this.clientLocator = clientLocator;
		this.serverLocator = serverLocator;
		this.playerProvider = playerProvider;
		this.queueCodec = NetworkCodec.queue(this.codec, ArrayDeque::new);
	}

	public static void createPacketChannels(Consumer<SyncChannel<?, ?>> register) {
		register.accept(BLOCK_ENTITY);
		register.accept(ENTITY);
		register.accept(CHUNK);
		register.accept(WORLD);
		register.accept(LEVEL);
	}

	@Environment(EnvType.CLIENT)
	public P toClientProvider(PacketByteBuf buf) {
		return this.clientLocator.apply(this.codec.decode(buf));
	}

	public PacketByteBuf toClientBuffer(P p) {
		var buf = PacketByteBufs.create();
		this.codec.encode(buf, this.identifyingDataTransformer.apply(p));
		return buf;
	}

	@SuppressWarnings("unchecked")
	public void send(Collection<ServerPlayerEntity> players, ComponentProvider provider, BufferFiller filler) {
		// The casting to P should never fail, since the provider and
		// the channel type *must* match for the implementation to be correct anyway
		P providerAsP = (P) provider;
		var buf = this.toClientBuffer(providerAsP); // append provider data
		filler.fill(buf);                                         // append all the container data
		ServerPlayNetworking.send(players.isEmpty() ? this.playerProvider.apply(providerAsP) : players, this.channelId, buf);
	}

	public void syncFromQueue(Queue<ComponentType<?>> pendingSync, ComponentProvider provider) {
		this.syncFromQueue(pendingSync, provider, List.of());
	}

	public void syncFromQueue(Queue<ComponentType<?>> pendingSync, ComponentProvider provider, Collection<ServerPlayerEntity> players) {
		if (pendingSync.isEmpty()) {
			return;
		}

		this.send(players, provider, buf -> { // calling 'send' appends the provider data
			buf.writeInt(pendingSync.size()); // append size

			while (!pendingSync.isEmpty()) {
				var currentType = pendingSync.poll();
				ComponentType.NETWORK_CODEC.encode(buf, currentType); // append type rawId
				((Syncable) provider.expose(currentType).unwrap()).writeToBuf(buf); // append component data
			}
		});
	}

	@Environment(EnvType.CLIENT)
	public void handleServerPushedSync(MinecraftClient client, PacketByteBuf buf) {
		buf.retain(); // hold the buffer in memory
		client.execute(() -> {
			// provider data is consumed by SyncChannel#toClientProvider
			ComponentProvider provider = this.toClientProvider(buf);

			int size = buf.readInt(); // consume size

			if (provider == null) {
				return;
			}

			for (int i = 0; i < size; i++) {
				ComponentType<?> type = ComponentType.NETWORK_CODEC.decode(buf); // consume rawId
				provider.expose(type).ifJust(component -> ((Syncable) component).readFromBuf(buf)); // consume component data
			}

			buf.release(); // make sure the buffer is properly freed
		});
	}

	public void handleClientSyncRequest(MinecraftServer server, ServerPlayerEntity sender, PacketByteBuf buf) {
		buf.retain();
		server.execute(() -> {
			Queue<U> queued = this.queueCodec.decode(buf); // we retrieve the queue of identifying data

			while (!queued.isEmpty()) {
				var identifyingData = queued.poll();

				ComponentProvider provider = this.serverLocator.apply(sender, identifyingData);
				if (provider == null) {
					return;
				}

				// force sync the target provider
				this.forceSync(provider, sender);
			}

			buf.release();
		});
	}

	/**
	 * Forcibly syncs all syncable components.
	 * Currently, used only for initial sync!
	 *
	 * @param provider The provider to force the sync on.
	 * @param sender   The client that requested the sync.
	 * @apiNote Avoid using this to sync components. Sync should be initiated from the server, unless specifically needed.
	 */
	public void forceSync(ComponentProvider provider, ServerPlayerEntity sender) {
		var queue = new ArrayDeque<ComponentType<?>>();

		provider.getComponentContainer().forEach((type, component) -> {
			if (component instanceof Syncable) {
				queue.add(type);
			}
		});

		this.syncFromQueue(queue, provider, Collections.singletonList(sender));
	}

	@Environment(EnvType.CLIENT)
	public void requestSync(P p) {
		this.requestQueue.add(this.identifyingDataTransformer.apply(p));
	}

	@Environment(EnvType.CLIENT)
	public void requestSync(U identifyingData) {
		this.requestQueue.add(identifyingData);
	}

	@Environment(EnvType.CLIENT)
	public void sendMassRequests() {
		if (this.requestQueue.isEmpty()) {
			return;
		}

		var buf = PacketByteBufs.create();
		this.queueCodec.encode(buf, this.requestQueue);
		this.requestQueue.clear();

		ClientPlayNetworking.send(this.channelId, buf);
	}

	public NetworkCodec<U> getCodec() {
		return this.codec;
	}

	public Function<P, U> getIdentifyingDataTransformer() {
		return this.identifyingDataTransformer;
	}

	public Function<U, P> getClientLocator() {
		return this.clientLocator;
	}

	public BiFunction<ServerPlayerEntity, U, P> getServerLocator() {
		return this.serverLocator;
	}

	public Function<? super P, Collection<ServerPlayerEntity>> getPlayerProvider() {
		return this.playerProvider;
	}

	public Identifier getChannelId() {
		return this.channelId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof SyncChannel<?, ?> that)) return false;
		return this.channelId.equals(that.channelId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.channelId, this.codec, this.identifyingDataTransformer, this.clientLocator, this.serverLocator, this.playerProvider);
	}

	@Override
	public String toString() {
		return "SyncChannel[" + this.channelId + "]";
	}

	@FunctionalInterface
	public interface BufferFiller {
		void fill(PacketByteBuf buf);
	}
}
