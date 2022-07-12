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

package org.quiltmc.qsl.component.impl.sync.codec;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.impl.client.ClientResolution;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public record NetworkCodec<T>(BiConsumer<PacketByteBuf, T> encoder,
							  Function<PacketByteBuf, T> decoder) {
	public static final NetworkCodec<Byte> BYTE = new NetworkCodec<>(
			(buf, aByte) -> buf.writeByte(aByte), PacketByteBuf::readByte
	);
	public static final NetworkCodec<Integer> INT = new NetworkCodec<>(
			PacketByteBuf::writeInt, PacketByteBuf::readInt
	);
	public static final NetworkCodec<Short> SHORT = new NetworkCodec<>(
			(buf, aShort) -> buf.writeShort(aShort), PacketByteBuf::readShort
	);
	public static final NetworkCodec<Long> LONG = new NetworkCodec<>(
			PacketByteBuf::writeLong, PacketByteBuf::readLong
	);
	public static final NetworkCodec<Float> FLOAT = new NetworkCodec<>(
			PacketByteBuf::writeFloat, PacketByteBuf::readFloat
	);
	public static final NetworkCodec<Double> DOUBLE = new NetworkCodec<>(
			PacketByteBuf::writeDouble, PacketByteBuf::readDouble
	);
	public static final NetworkCodec<String> STRING = new NetworkCodec<>(
			PacketByteBuf::writeString, PacketByteBuf::readString
	);
	public static final NetworkCodec<Identifier> IDENTIFIER = new NetworkCodec<>(
			PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier
	);
	public static final NetworkCodec<NbtCompound> NBT_COMPOUND = new NetworkCodec<>(
			PacketByteBuf::writeNbt, PacketByteBuf::readNbt
	);
	public static final NetworkCodec<UUID> UUID = new NetworkCodec<>(
			PacketByteBuf::writeUuid, PacketByteBuf::readUuid
	);
	public static final NetworkCodec<ChunkPos> CHUNK_POS = new NetworkCodec<>(
			PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos
	);
	public static final NetworkCodec<BlockPos> BLOCK_POS = new NetworkCodec<>(
			PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos
	);
	public static final NetworkCodec<ItemStack> ITEM_STACK = new NetworkCodec<>(
			PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack
	);
	public static final NetworkCodec<DefaultedList<ItemStack>> INVENTORY =
			list(ITEM_STACK, size -> DefaultedList.ofSize(size, ItemStack.EMPTY));
	public static final NetworkCodec<BlockEntity> BLOCK_ENTITY =
			BLOCK_POS.map(BlockEntity::getPos, ClientResolution::getBlockEntity);
	public static final NetworkCodec<Entity> ENTITY =
			INT.map(Entity::getId, ClientResolution::getEntity);
	public static final NetworkCodec<Chunk> CHUNK =
			CHUNK_POS.map(Chunk::getPos, ClientResolution::getChunk);

	public static <O, L extends List<O>> NetworkCodec<L> list(NetworkCodec<O> entryCodec, IntFunction<L> listFactory) {
		return new NetworkCodec<>(
				(buf, os) -> {
					INT.encode(buf, os.size());
					os.forEach(o -> entryCodec.encode(buf, o));
				},
				buf -> {
					int size = INT.decode(buf).unwrap();
					L newList = listFactory.apply(size);

					for (int i = 0; i < size; i++) {
						newList.set(i, entryCodec.decode(buf).unwrap());
					}

					return newList;
				}
		);
	}

	public static <O, V, M extends Map<O, V>> NetworkCodec<M> map(NetworkCodec<Pair<O, V>> entryCodec, IntFunction<M> mapFactory) {
		return new NetworkCodec<>(
				(buf, m) -> {
					INT.encode(buf, m.size());
					m.forEach((key, value) -> entryCodec.encode(buf, Pair.of(key, value)));
				},
				buf -> {
					int size = INT.decode(buf).unwrap();
					var map = mapFactory.apply(size);
					for (int i = 0; i < size; i++) {
						entryCodec.decode(buf).ifJust(ovPair -> map.put(ovPair.getFirst(), ovPair.getSecond()));
					}

					return map;
				}
		);
	}

	public static <K, V> NetworkCodec<Pair<K, V>> pair(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec) {
		return new NetworkCodec<>(
				(buf, kvPair) -> {
					keyCodec.encode(buf, kvPair.getFirst());
					valueCodec.encode(buf, kvPair.getSecond());
				},
				buf -> Pair.of(keyCodec.decoder.apply(buf), valueCodec.decoder.apply(buf))
		);
	}

	public void encode(PacketByteBuf buf, T t) {
		this.encoder.accept(buf, t);
	}

	public Maybe<T> decode(PacketByteBuf buf) {
		return Maybe.wrap(this.decoder.apply(buf));
	}

	public <U> NetworkCodec<U> map(Function<U, T> decoder, Function<T, U> encoder) {
		return new NetworkCodec<>(
				(buf, u) -> this.encoder.accept(buf, decoder.apply(u)),
				buf -> encoder.apply(this.decoder.apply(buf))
		);
	}

	public record Thingy(Identifier id, String idString, int twelve) {
	}
}
