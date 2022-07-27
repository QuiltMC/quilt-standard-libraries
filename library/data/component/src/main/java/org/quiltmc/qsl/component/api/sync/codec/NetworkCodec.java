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

package org.quiltmc.qsl.component.api.sync.codec;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public record NetworkCodec<T>(BiConsumer<PacketByteBuf, T> encoder, Function<PacketByteBuf, T> decoder) {
	public static final NetworkCodec<Byte> BYTE = new NetworkCodec<>(
			(buf, aByte) -> buf.writeByte(aByte), PacketByteBuf::readByte
	);
	public static final NetworkCodec<Integer> INT = new NetworkCodec<>(
			PacketByteBuf::writeInt, PacketByteBuf::readInt
	);
	public static final NetworkCodec<Integer> VAR_INT = new NetworkCodec<>(
			PacketByteBuf::writeVarInt, PacketByteBuf::readVarInt
	);
	public static final NetworkCodec<Short> SHORT = new NetworkCodec<>(
			(buf, aShort) -> buf.writeShort(aShort), PacketByteBuf::readShort
	);
	public static final NetworkCodec<Long> LONG = new NetworkCodec<>(
			PacketByteBuf::writeLong, PacketByteBuf::readLong
	);
	public static final NetworkCodec<Long> VAR_LONG = new NetworkCodec<>(
			PacketByteBuf::writeVarLong, PacketByteBuf::readVarLong
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
	public static final NetworkCodec<Unit> EMPTY = new NetworkCodec<>((buf, unit) -> { }, buf -> Unit.INSTANCE);

	public static <O, L extends List<O>> NetworkCodec<L> list(NetworkCodec<O> entryCodec, IntFunction<L> listFactory) {
		return new NetworkCodec<>(
				(buf, os) -> {
					VAR_INT.encode(buf, os.size());
					os.forEach(o -> entryCodec.encode(buf, o));
				},
				buf -> {
					int size = VAR_INT.decode(buf);
					L newList = listFactory.apply(size);

					for (int i = 0; i < size; i++) {
						newList.set(i, entryCodec.decode(buf));
					}

					return newList;
				}
		);
	}

	public static <O, Q extends Queue<O>> NetworkCodec<Q> queue(NetworkCodec<O> elementCodec, IntFunction<Q> queueFactory) {
		return new NetworkCodec<>(
				(buf, os) -> {
					VAR_INT.encode(buf, os.size());
					os.forEach(o -> elementCodec.encode(buf, o));
				},
				buf -> {
					int size = VAR_INT.decode(buf);
					var queue = queueFactory.apply(size);

					for (int i = 0; i < size; i++) {
						queue.add(elementCodec.decode(buf));
					}

					return queue;
				}
		);
	}

	public static <O, V, M extends Map<O, V>> NetworkCodec<M> map(NetworkCodec<O> keyCodec, NetworkCodec<V> valueCodec, IntFunction<M> mapFactory) {
		return new NetworkCodec<>(
				(buf, map) -> {
					VAR_INT.encode(buf, map.size()); // append size
					map.forEach((o, v) -> {
						keyCodec.encode(buf, o); // append key
						valueCodec.encode(buf, v); // append value
					});
				},
				buf -> {
					int size = VAR_INT.decode(buf); // consume size
					var map = mapFactory.apply(size);

					for (int i = 0; i < size; i++) {
						map.put(keyCodec.decode(buf), valueCodec.decode(buf)); // consume key-value pair
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

	public static <T> NetworkCodec<T> idIndexed(IndexedIterable<T> idList) {
		return NetworkCodec.VAR_INT.map(idList::getRawId, idList::get);
	}

	public static <T> NetworkCodec<T> defaultReturn(Supplier<T> instanceProvider) {
		return new NetworkCodec<>(
				(buf, t) -> {
				},
				buf -> instanceProvider.get()
		);
	}

	public void encode(PacketByteBuf buf, T t) {
		this.encoder.accept(buf, t);
	}

	public T decode(PacketByteBuf buf) {
		try {
			return this.decoder.apply(buf);
		} catch (IndexOutOfBoundsException e) { // An IOOB exception is thrown if we try to read invalid memory
			CrashReport report = new CrashReport("Attempted to read invalid memory!", e);
			throw new CrashException(report);
		}
	}

	public <U> NetworkCodec<U> map(Function<U, T> decoder, Function<T, U> encoder) {
		return new NetworkCodec<>(
				(buf, u) -> this.encoder.accept(buf, decoder.apply(u)),
				buf -> encoder.apply(this.decoder.apply(buf))
		);
	}
}
