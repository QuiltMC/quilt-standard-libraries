package org.quiltmc.qsl.component.impl.sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.quiltmc.qsl.base.api.util.TwoWayFunction;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.client.sync.ClientResolution;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public record SyncChannel<P extends ComponentProvider>(Identifier channelId,
													   NetworkCodec<P> codec,
													   Function<? super P, Collection<ServerPlayerEntity>> playerProvider)
													   implements TwoWayFunction<P, PacketByteBuf> {
	// BlockEntity
	private static final NetworkCodec<BlockEntity> BLOCK_ENTITY_CODEC =
			NetworkCodec.BLOCK_POS.map(BlockEntity::getPos, ClientResolution::getBlockEntity);
	public static final SyncChannel<BlockEntity> BLOCK_ENTITY =
			new SyncChannel<>(PacketIds.BLOCK_ENTITY_SYNC, BLOCK_ENTITY_CODEC, PlayerLookup::tracking);

	// Entity
	private static final NetworkCodec<Entity> ENTITY_CODEC =
			NetworkCodec.INT.map(Entity::getId, ClientResolution::getEntity);
	public static final SyncChannel<Entity> ENTITY =
			new SyncChannel<>(PacketIds.ENTITY_SYNC, ENTITY_CODEC, PlayerLookup::tracking);

	// Chunk
	private static final NetworkCodec<Chunk> CHUNK_CODEC =
			NetworkCodec.CHUNK_POS.map(Chunk::getPos, ClientResolution::getChunk);
	public static final SyncChannel<Chunk> CHUNK = // **Careful**: This only works with WorldChunks not other chunk types!
			new SyncChannel<>(PacketIds.CHUNK_SYNC, CHUNK_CODEC,
				  chunk -> PlayerLookup.tracking((ServerWorld) ((WorldChunk) chunk).getWorld(), chunk.getPos()));

	// World
	private static final NetworkCodec<World> WORLD_CODEC =
			NetworkCodec.empty(ClientResolution::getWorld);
	public static final SyncChannel<World> WORLD =
			new SyncChannel<>(PacketIds.WORLD_SYNC, WORLD_CODEC,
				  world -> PlayerLookup.world(((ServerWorld) world)));

	// Level
	private static final NetworkCodec<ComponentProvider> LEVEL_CODEC =
			NetworkCodec.empty(MinecraftClient::getInstance);
	public static final SyncChannel<?> LEVEL =
			new SyncChannel<>(PacketIds.LEVEL_SYNC, LEVEL_CODEC,
				  provider -> PlayerLookup.all(((MinecraftServer) provider)));

	public static void createPacketChannels(Consumer<? super SyncChannel<?>> register) {
		register.accept(BLOCK_ENTITY);
		register.accept(ENTITY);
		register.accept(CHUNK);
		register.accept(WORLD);
		register.accept(LEVEL);
	}

	@Override
	public P to(PacketByteBuf buf) {
		return this.codec.decode(buf).unwrap(); // we throw in case we cannot find the target component provider.
	}

	@Override
	public PacketByteBuf from(P p) {
		var buf = PacketByteBufs.create();
		this.codec.encode(buf, p);
		return buf;
	}

	@SuppressWarnings("unchecked")
	public void send(ComponentProvider provider, BufferFiller bufferFiller) {
		// The casting to P should never fail, since the provider and
		// the channel type *must* match for the implementation to be correct anyway
		P providerAsP = (P) provider;
		var buf = this.from(providerAsP); // append provider data
		bufferFiller.fill(buf); 					    // append all the container data
		ServerPlayNetworking.send(this.playerProvider.apply(providerAsP), this.channelId, buf);
	}

	@FunctionalInterface
	public interface BufferFiller {
		void fill(PacketByteBuf buf);
	}
}
