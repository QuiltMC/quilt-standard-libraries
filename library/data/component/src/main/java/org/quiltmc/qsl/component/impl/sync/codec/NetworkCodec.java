package org.quiltmc.qsl.component.impl.sync.codec;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record NetworkCodec<T>(@NotNull BiConsumer<PacketByteBuf, T> encoder,
							  @NotNull Function<PacketByteBuf, T> decoder) {
	public static final NetworkCodec<BlockPos> BLOCK_POS = new NetworkCodec<>(
			PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos
	);
	public static final NetworkCodec<Integer> INT = new NetworkCodec<>(
			PacketByteBuf::writeInt, PacketByteBuf::readInt
	);
	public static final NetworkCodec<ChunkPos> CHUNK_POS = new NetworkCodec<>(
			PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos
	);
	public static final NetworkCodec<Float> FLOAT = new NetworkCodec<>(
			PacketByteBuf::writeFloat, PacketByteBuf::readFloat
	);
	// TODO: Handle the cases where MinecraftClient.getInstance().world is null
	public static final NetworkCodec<BlockEntity> BLOCK_ENTITY = new NetworkCodec<>(
			(buf, blockEntity) -> buf.writeBlockPos(blockEntity.getPos()),
			buf -> MinecraftClient.getInstance().world.getBlockEntity(buf.readBlockPos())
	);
	public static final NetworkCodec<Entity> ENTITY = new NetworkCodec<>(
			(buf, entity) -> buf.writeInt(entity.getId()),
			buf -> MinecraftClient.getInstance().world.getEntityById(buf.readInt())
	);
	public static final NetworkCodec<Chunk> CHUNK = new NetworkCodec<>(
			(buf, chunk) -> buf.writeChunkPos(chunk.getPos()),
			buf -> CHUNK_POS.decode(buf)
					.map(chunkPos -> MinecraftClient.getInstance().world.getChunk(chunkPos.x, chunkPos.z))
					.orElseThrow()
	);
	public static final NetworkCodec<ItemStack> ITEMSTACK = new NetworkCodec<>(
			PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack
	);
	public static final NetworkCodec<Long> LONG = new NetworkCodec<>(
			PacketByteBuf::writeLong, PacketByteBuf::readLong
	);
	public static final NetworkCodec<String> STRING = new NetworkCodec<>(
			PacketByteBuf::writeString, PacketByteBuf::readString
	);
	public static final NetworkCodec<Identifier> IDENTIFIER = new NetworkCodec<>(
			PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier
	);
	public static final NetworkCodec<DefaultedList<ItemStack>> INVENTORY = list(ITEMSTACK, integer -> DefaultedList.ofSize(integer, ItemStack.EMPTY));
	public static final NetworkCodec<NbtCompound> NBT_COMPOUND = new NetworkCodec<>(
			PacketByteBuf::writeNbt, PacketByteBuf::readNbt
	);
	public static final NetworkCodec<UUID> UUID = new NetworkCodec<>(
			PacketByteBuf::writeUuid, PacketByteBuf::readUuid
	);
	public static final NetworkCodec<ComponentType<?>> COMPONENT_TYPE = new NetworkCodec<>(
			(buf, componentType) -> buf.writeInt(Components.REGISTRY.getRawId(componentType)),
			buf -> ClientSyncHandler.getInstance().getType(buf.readInt())
	);

	public static <O, L extends List<O>> NetworkCodec<L> list(NetworkCodec<O> entryCodec, Function<Integer, L> function) {
		return new NetworkCodec<>(
				(buf, os) -> {
					INT.encode(buf, os.size());
					for (O o : os) {
						entryCodec.encode(buf, o);
					}
				},
				buf -> {
					int size = INT.decode(buf).orElseThrow();
					L newList = function.apply(size);

					for (int i = 0; i < size; i++) {
						newList.set(i, entryCodec.decode(buf).orElseThrow());
					}

					return newList;
				}
		);
	}

	public void encode(@NotNull PacketByteBuf buf, T t) {
		this.encoder.accept(buf, t);
	}

	public Optional<T> decode(@NotNull PacketByteBuf buf) {
		return Optional.ofNullable(this.decoder.apply(buf));
	}
}
