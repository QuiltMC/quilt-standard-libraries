package org.quiltmc.qsl.networking.api.codec;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

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
	NetworkCodec<String> STRING = of(PacketByteBuf::readString, PacketByteBuf::writeString, "String");
	NetworkCodec<UUID> UUID = of(PacketByteBuf::readUuid, PacketByteBuf::writeUuid, "UUID");
	NetworkCodec<BitSet> BIT_SET = of(PacketByteBuf::readBitSet, PacketByteBuf::writeBitSet, "BitSet");
	NetworkCodec<NbtCompound> NBT = of(PacketByteBuf::readNbt, PacketByteBuf::writeNbt, "NBT");
	NetworkCodec<ItemStack> ITEM_STACK = of(PacketByteBuf::readItemStack, PacketByteBuf::writeItemStack, "ItemStack");

	static <A> NetworkCodec<A> of(PacketByteBuf.Reader<A> decoder, PacketByteBuf.Writer<A> encoder, String name) {
		return new SimpleNetworkCodec<>(decoder, encoder, name);
	}

	static <A> NetworkCodec<A> of(PacketByteBuf.Reader<A> decoder, PacketByteBuf.Writer<A> encoder) {
		return new SimpleNetworkCodec<>(decoder, encoder, null);
	}

	static <A> NetworkCodec<List<A>> listOf(NetworkCodec<A> entryCodec, IntFunction<? extends List<A>> listFactory) {
		return new ListNetworkCodec<>(entryCodec, listFactory);
	}

	static <K, V> NetworkCodec<Map<K, V>> mapOf(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec, IntFunction<? extends Map<K, V>> mapFactory) {
		return new MapNetworkCodec<>(keyCodec, valueCodec, mapFactory);
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
				byteBuf -> to.apply(this.decode(byteBuf)),
				(byteBuf, data) -> this.encode(byteBuf, from.apply(data)),
				this + "[mapped]"
		);
	}
}
