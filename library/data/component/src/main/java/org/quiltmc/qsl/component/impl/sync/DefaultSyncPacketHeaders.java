package org.quiltmc.qsl.component.impl.sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;

@SuppressWarnings("ConstantConditions") // The accessed fields in this case will never be null since the only times this class is accessed are during play.
public final class DefaultSyncPacketHeaders {
	private static final NetworkCodec<BlockEntity> BLOCK_ENTITY_CODEC = new NetworkCodec<>(
			(blockEntity, packetByteBuf) -> packetByteBuf.writeBlockPos(blockEntity.getPos()),
			packetByteBuf -> MinecraftClient.getInstance().world.getBlockEntity(packetByteBuf.readBlockPos())
	);
	public static final SyncPacketHeader<BlockEntity> BLOCK_ENTITY = new SyncPacketHeader<>(BLOCK_ENTITY_CODEC);
	private static final NetworkCodec<Entity> ENTITY_CODEC = new NetworkCodec<>(
			(entity, packetByteBuf) -> packetByteBuf.writeInt(entity.getId()),
			packetByteBuf -> MinecraftClient.getInstance().world.getEntityById(packetByteBuf.readInt())
	);
	public static final SyncPacketHeader<Entity> ENTITY = new SyncPacketHeader<>(ENTITY_CODEC);
	private static final NetworkCodec<Chunk> CHUNK_CODEC = new NetworkCodec<>(
			(chunk, packetByteBuf) -> packetByteBuf.writeChunkPos(chunk.getPos()),
			packetByteBuf -> {
				ChunkPos pos = packetByteBuf.readChunkPos();
				return MinecraftClient.getInstance().world.getChunk(pos.x, pos.z);
			}
	);
	public static final SyncPacketHeader<Chunk> CHUNK = new SyncPacketHeader<>(CHUNK_CODEC);
	// TODO: LevelProperties sync

	public static void registerDefaults() {
		ComponentHeaderRegistry.register(CommonInitializer.id("block_entity"), BLOCK_ENTITY);
		ComponentHeaderRegistry.register(CommonInitializer.id("entity"), ENTITY);
		ComponentHeaderRegistry.register(CommonInitializer.id("chunk"), CHUNK);
	}
}
