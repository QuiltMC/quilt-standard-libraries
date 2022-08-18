package org.quiltmc.qsl.networking.api.codec;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.mojang.datafixers.util.Unit;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.impl.codec.ArrayNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.ListNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.MapNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.OptionalNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.PrimitiveNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.SimpleNetworkCodec;

public interface NetworkCodec<A> {
	// Unit
	NetworkCodec<Unit> UNIT = constant(Unit.INSTANCE);

	// Java Primitives
	PrimitiveNetworkCodec.Boolean BOOLEAN = new PrimitiveNetworkCodec.Boolean();
	PrimitiveNetworkCodec.Byte BYTE = new PrimitiveNetworkCodec.Byte();
	PrimitiveNetworkCodec.Char CHAR = new PrimitiveNetworkCodec.Char();
	PrimitiveNetworkCodec.Short SHORT = new PrimitiveNetworkCodec.Short();
	PrimitiveNetworkCodec.Int INT = new PrimitiveNetworkCodec.Int();
	PrimitiveNetworkCodec.VarInt VAR_INT = new PrimitiveNetworkCodec.VarInt();
	PrimitiveNetworkCodec.Long LONG = new PrimitiveNetworkCodec.Long();
	PrimitiveNetworkCodec.VarLong VAR_LONG = new PrimitiveNetworkCodec.VarLong();

	// General
	NetworkCodec<String> STRING = of(PacketByteBuf::writeString, PacketByteBuf::readString, "String");
	NetworkCodec<UUID> UUID = of(PacketByteBuf::writeUuid, PacketByteBuf::readUuid, "UUID");
	NetworkCodec<BitSet> BIT_SET = of(PacketByteBuf::writeBitSet, PacketByteBuf::readBitSet, "BitSet");
	NetworkCodec<NbtCompound> NBT = of(PacketByteBuf::writeNbt, PacketByteBuf::readNbt, "NBT");
	NetworkCodec<ItemStack> ITEM_STACK = of(PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack, "ItemStack");

	static <A> NetworkCodec<A> of(PacketByteBuf.Writer<A> encoder, PacketByteBuf.Reader<A> decoder, String name) {
		return new SimpleNetworkCodec<>(encoder, decoder, name);
	}

	static <A> NetworkCodec<A> of(PacketByteBuf.Writer<A> encoder, PacketByteBuf.Reader<A> decoder) {
		return new SimpleNetworkCodec<>(encoder, decoder, null);
	}

	static <A> NetworkCodec<List<A>> listOf(NetworkCodec<A> entryCodec, IntFunction<? extends List<A>> listFactory) {
		return new ListNetworkCodec<>(entryCodec, listFactory);
	}

	static <K, V> NetworkCodec<Map<K, V>> mapOf(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec, IntFunction<? extends Map<K, V>> mapFactory) {
		return new MapNetworkCodec<>(entryOf(keyCodec, valueCodec), mapFactory);
	}

	static <K, V> NetworkCodec<Map<K, V>> mapOf(NetworkCodec<Map.Entry<K, V>> entryCodec, IntFunction<? extends Map<K, V>> mapFactory) {
		return new MapNetworkCodec<>(entryCodec, mapFactory);
	}

	static <A> NetworkCodec<A[]> arrayOf(NetworkCodec<A> entryCodec, IntFunction<? extends A[]> arrayFactory) {
		return new ArrayNetworkCodec<>(entryCodec, arrayFactory);
	}

	static <K, V> NetworkCodec<Map.Entry<K, V>> entryOf(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec) {
		return new MapNetworkCodec.EntryCodec<>(keyCodec, valueCodec);
	}

	static <A> NetworkCodec<Optional<A>> optionalOf(NetworkCodec<A> codec) {
		return new OptionalNetworkCodec<>(codec);
	}

	static <A> NetworkCodec<A> constant(A value) {
		return of((buf, a) -> {}, (buf) -> value, "Constant[" + value + "]");
	}

	A decode(PacketByteBuf buf);

	void encode(PacketByteBuf buf, A data);

	default PacketByteBuf.Reader<A> asReader() {
		return this::decode;
	}

	default PacketByteBuf.Writer<A> asWriter() {
		return this::encode;
	}

	default <B> NetworkCodec<B> map(Function<? super B, ? extends A> from, Function<? super A, ? extends B> to) {
		return new SimpleNetworkCodec<>(
				(byteBuf, data) -> this.encode(byteBuf, from.apply(data)), byteBuf -> to.apply(this.decode(byteBuf)),
				this + "[mapped]"
		);
	}
}
