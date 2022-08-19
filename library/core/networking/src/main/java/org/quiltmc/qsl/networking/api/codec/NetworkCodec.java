package org.quiltmc.qsl.networking.api.codec;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Unit;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.impl.codec.ArrayNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.DispatchedNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.EitherNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.EnumNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.ListNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.MapNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.NamedNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.NetworkCodecBuilder;
import org.quiltmc.qsl.networking.impl.codec.OptionalNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.PairNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.PrimitiveNetworkCodec;
import org.quiltmc.qsl.networking.impl.codec.SimpleNetworkCodec;

public interface NetworkCodec<A> {
	// Unit
	NetworkCodec<Unit> UNIT = constant(Unit.INSTANCE);

	// Java Primitives
	PrimitiveNetworkCodec.Null NULL = PrimitiveNetworkCodec.Null.INSTANCE;
	PrimitiveNetworkCodec.Boolean BOOLEAN = PrimitiveNetworkCodec.Boolean.INSTANCE;
	PrimitiveNetworkCodec.Byte BYTE = PrimitiveNetworkCodec.Byte.INSTANCE;
	PrimitiveNetworkCodec.Short SHORT = PrimitiveNetworkCodec.Short.INSTANCE;
	PrimitiveNetworkCodec.Int INT = PrimitiveNetworkCodec.Int.INSTANCE;
	PrimitiveNetworkCodec.VarInt VAR_INT = PrimitiveNetworkCodec.VarInt.INSTANCE;
	PrimitiveNetworkCodec.Long LONG = PrimitiveNetworkCodec.Long.INSTANCE;
	PrimitiveNetworkCodec.VarLong VAR_LONG = PrimitiveNetworkCodec.VarLong.INSTANCE;
	PrimitiveNetworkCodec.Float FLOAT = PrimitiveNetworkCodec.Float.INSTANCE;
	PrimitiveNetworkCodec.Double DOUBLE = PrimitiveNetworkCodec.Double.INSTANCE;

	// Useful Java Types
	NetworkCodec<byte[]> BYTE_ARRAY = of(PacketByteBuf::writeByteArray, PacketByteBuf::readByteArray).named("ByteArray");
	NetworkCodec<long[]> LONG_ARRAY = of(PacketByteBuf::writeLongArray, PacketByteBuf::readLongArray).named("LongArray");
	NetworkCodec<String> STRING = of(PacketByteBuf::writeString, PacketByteBuf::readString).named("String");
	NetworkCodec<UUID> UUID = of(PacketByteBuf::writeUuid, PacketByteBuf::readUuid).named("UUID");
	NetworkCodec<BitSet> BIT_SET = of(PacketByteBuf::writeBitSet, PacketByteBuf::readBitSet).named("BitSet");
	NetworkCodec<Date> DATE = of(PacketByteBuf::writeDate, PacketByteBuf::readDate).named("Date");
	NetworkCodec<Instant> INSTANT = of(PacketByteBuf::writeInstant, PacketByteBuf::readInstant).named("Instant");

	// Minecraft Types
	NetworkCodec<Identifier> IDENTIFIER = of(PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier)
			.named("Identifier");
	NetworkCodec<NbtCompound> NBT = of(PacketByteBuf::writeNbt, PacketByteBuf::readNbt)
			.named("NBT");
	NetworkCodec<ItemStack> ITEM_STACK = of(PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack)
			.named("ItemStack");
	NetworkCodec<Vec3i> VEC_3I = NetworkCodec.<Vec3i>builder().create(
			VAR_INT.fieldOf(Vec3i::getX),
			VAR_INT.fieldOf(Vec3i::getY),
			VAR_INT.fieldOf(Vec3i::getZ)
	).apply(Vec3i::new).named("Vec3i");
	NetworkCodec<Direction> DIRECTION = enumOf(Direction.class)
			.named("Direction");
	NetworkCodec<BlockPos> BLOCK_POS = of(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos)
			.named("BlockPos");
	NetworkCodec<ChunkPos> CHUNK_POS = of(PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos)
			.named("ChunkPos");
	NetworkCodec<GlobalPos> GLOBAL_POS = of(PacketByteBuf::writeGlobalPos, PacketByteBuf::readGlobalPos)
			.named("GlobalPos");
	NetworkCodec<Vec3d> VEC_3D = NetworkCodec.<Vec3d>builder().create(
			DOUBLE.fieldOf(Vec3d::getX),
			DOUBLE.fieldOf(Vec3d::getY),
			DOUBLE.fieldOf(Vec3d::getZ)
	).apply(Vec3d::new).named("Vec3d");
	NetworkCodec<Vec3f> VEC_3F = NetworkCodec.<Vec3f>builder().create(
			FLOAT.fieldOf(Vec3f::getX),
			FLOAT.fieldOf(Vec3f::getY),
			FLOAT.fieldOf(Vec3f::getZ)
	).apply(Vec3f::new).named("Vec3f");
	NetworkCodec<Text> TEXT = of(PacketByteBuf::writeText, PacketByteBuf::readText)
			.named("Text");
	NetworkCodec<Property> PROPERTY = of(PacketByteBuf::writeProperty, PacketByteBuf::readProperty)
			.named("Property");
	NetworkCodec<BlockHitResult> BLOCK_HIT_RESULT = of(
			PacketByteBuf::writeBlockHitResult, PacketByteBuf::readBlockHitResult
	).named("BlockHitResult");

	static <A> NetworkCodec<A> of(PacketByteBuf.Writer<A> encoder, PacketByteBuf.Reader<A> decoder) {
		return new SimpleNetworkCodec<>(encoder, decoder);
	}

	static <A> ListNetworkCodec<A> listOf(NetworkCodec<A> entryCodec, IntFunction<? extends List<A>> listFactory) {
		return new ListNetworkCodec<>(entryCodec, listFactory);
	}

	static <K, V> MapNetworkCodec<K, V> mapOf(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec,
			IntFunction<? extends Map<K, V>> mapFactory) {
		return mapOf(entryOf(keyCodec, valueCodec), mapFactory);
	}

	static <K, V> MapNetworkCodec<K, V> mapOf(MapNetworkCodec.EntryCodec<K, V> entryCodec,
			IntFunction<? extends Map<K, V>> mapFactory) {
		return new MapNetworkCodec<>(entryCodec, mapFactory);
	}

	static <A> ArrayNetworkCodec<A> arrayOf(NetworkCodec<A> entryCodec, IntFunction<? extends A[]> arrayFactory) {
		return new ArrayNetworkCodec<>(entryCodec, arrayFactory);
	}

	static <K, V> MapNetworkCodec.EntryCodec<K, V> entryOf(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec) {
		return new MapNetworkCodec.EntryCodec<>(keyCodec, valueCodec);
	}

	static <A, B> PairNetworkCodec<A, B> pairOf(NetworkCodec<A> aCodec, NetworkCodec<B> bCodec) {
		return new PairNetworkCodec<>(aCodec, bCodec);
	}

	static <A> OptionalNetworkCodec<A> optionalOf(NetworkCodec<A> codec) {
		return new OptionalNetworkCodec<>(codec);
	}

	static <A> NetworkCodec<A> indexOf(IndexedIterable<A> idxIter) {
		return VAR_INT.mapInt(idxIter::getRawId, idxIter::get).named("IndexOf[%s]".formatted(idxIter));
	}

	static <A, B> EitherNetworkCodec<A, B> eitherOf(NetworkCodec<A> leftCodec, NetworkCodec<B> rightCodec) {
		return new EitherNetworkCodec<>(leftCodec, rightCodec);
	}

	static <A> NetworkCodec<A> constant(A value) {
		return of((buf, a) -> { }, (buf) -> value).named("Constant[%s]".formatted(value));
	}

	static <A extends Enum<A>> EnumNetworkCodec<A> enumOf(Class<A> enumClass) {
		return new EnumNetworkCodec<>(enumClass);
	}

	static <A> NetworkCodecBuilder<A> builder() {
		return new NetworkCodecBuilder<>();
	}

	A decode(PacketByteBuf buf);

	void encode(PacketByteBuf buf, A data);

	default PacketByteBuf createBuffer(A data) {
		var buf = PacketByteBufs.create();
		this.encode(buf, data);
		return buf;
	}

	default PacketByteBuf.Reader<A> asReader() {
		return this::decode;
	}

	default PacketByteBuf.Writer<A> asWriter() {
		return this::encode;
	}

	default OptionalNetworkCodec<A> optional() {
		return optionalOf(this);
	}

	default ListNetworkCodec<A> list() {
		return this.list(ArrayList::new);
	}

	default ListNetworkCodec<A> list(IntFunction<? extends List<A>> listFactory) {
		return listOf(this, listFactory);
	}

	default <V> MapNetworkCodec.EntryCodec<A, V> zip(NetworkCodec<V> valueCodec) {
		return entryOf(this, valueCodec);
	}

	default <B> PairNetworkCodec<A, B> pairWith(NetworkCodec<B> secondCodec) {
		return pairOf(this, secondCodec);
	}

	default <B> NetworkCodec<B> map(Function<? super B, ? extends A> from, Function<? super A, ? extends B> to) {
		return new SimpleNetworkCodec<B>(
				(byteBuf, data) -> this.encode(byteBuf, from.apply(data)),
				byteBuf -> to.apply(this.decode(byteBuf))
		).named(this + " [mapped]");
	}

	default <T> NetworkCodecBuilder.Field<T, A> fieldOf(Function<? super T, ? extends A> fieldLocator) {
		return new NetworkCodecBuilder.Field<>(this, fieldLocator);
	}

	default NamedNetworkCodec<A> named(String name) {
		return new NamedNetworkCodec<>(name, this);
	}

	default <B> DispatchedNetworkCodec<B, A> dispatch(
			Function<? super B, A> transformer,
			Function<? super A, NetworkCodec<B>> dispatch) {
		return new DispatchedNetworkCodec<>(this, transformer, dispatch);
	}

	default NetworkCodec<A> base() {
		return this;
	}
}
