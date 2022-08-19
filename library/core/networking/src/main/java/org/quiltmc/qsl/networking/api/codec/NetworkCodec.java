package org.quiltmc.qsl.networking.api.codec;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.mojang.datafixers.util.Unit;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.impl.codec.ArrayNetworkCodec;
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
	PrimitiveNetworkCodec.Boolean BOOLEAN = new PrimitiveNetworkCodec.Boolean();
	PrimitiveNetworkCodec.Byte BYTE = new PrimitiveNetworkCodec.Byte();
	PrimitiveNetworkCodec.Char CHAR = new PrimitiveNetworkCodec.Char();
	PrimitiveNetworkCodec.Short SHORT = new PrimitiveNetworkCodec.Short();
	PrimitiveNetworkCodec.Int INT = new PrimitiveNetworkCodec.Int();
	PrimitiveNetworkCodec.VarInt VAR_INT = new PrimitiveNetworkCodec.VarInt();
	PrimitiveNetworkCodec.Float FLOAT = new PrimitiveNetworkCodec.Float();
	PrimitiveNetworkCodec.Long LONG = new PrimitiveNetworkCodec.Long();
	PrimitiveNetworkCodec.VarLong VAR_LONG = new PrimitiveNetworkCodec.VarLong();
	PrimitiveNetworkCodec.Double DOUBLE = new PrimitiveNetworkCodec.Double();

	// Useful Java Types
	NetworkCodec<String> STRING = of(PacketByteBuf::writeString, PacketByteBuf::readString).named("String");
	NetworkCodec<UUID> UUID = of(PacketByteBuf::writeUuid, PacketByteBuf::readUuid).named("UUID");
	NetworkCodec<BitSet> BIT_SET = of(PacketByteBuf::writeBitSet, PacketByteBuf::readBitSet).named("BitSet");

	// Minecraft Types
	NetworkCodec<NbtCompound> NBT = of(PacketByteBuf::writeNbt, PacketByteBuf::readNbt).named("NBT");
	NetworkCodec<ItemStack> ITEM_STACK = of(PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack)
			.named("ItemStack");
	NetworkCodec<Vec3i> VEC_3I = NetworkCodec.<Vec3i>builder().create(
			VAR_INT.fieldOf(Vec3i::getX),
			VAR_INT.fieldOf(Vec3i::getY),
			VAR_INT.fieldOf(Vec3i::getZ)
	).apply(Vec3i::new).named("Vec3i");
	NetworkCodec<BlockPos> BLOCK_POS = of(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos).named("BlockPos");
	NetworkCodec<Direction> DIRECTION = enumOf(Direction.values()).named("Direction");
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

	NetworkCodec<ItemStack> ITEMSTACK_2 = NetworkCodec.<ItemStack>builder().create(
			BOOLEAN.fieldOf(ItemStack::isEmpty),
			indexOf(Registry.ITEM).fieldOf(ItemStack::getItem),
			BYTE.fieldOf(stack -> (byte)stack.getCount()),
			NBT.optional().fieldOf(stack -> Optional.ofNullable(stack.getNbt()))
	).apply((isEmpty, item, count, nbt) -> {
		if (isEmpty) {
			return ItemStack.EMPTY;
		} else {
			var stack = new ItemStack(item, count);
			nbt.ifPresent(stack::setNbt);

			return stack;
		}
	}).named("ItemStack");

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

	static <A extends Enum<A>> EnumNetworkCodec<A> enumOf(A[] values) {
		return new EnumNetworkCodec<>(values);
	}

	static <A> NetworkCodecBuilder<A> builder() {
		return new NetworkCodecBuilder<>();
	}

	static <A, B> PairNetworkCodec<A, B> pairOf(NetworkCodec<A> aCodec, NetworkCodec<B> bCodec) {
		return new PairNetworkCodec<>(aCodec, bCodec);
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
		return listOf(this, ArrayList::new);
	}

	default <V> MapNetworkCodec.EntryCodec<A, V> zip(NetworkCodec<V> valueCodec) {
		return entryOf(this, valueCodec);
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

	default <B> PairNetworkCodec<A, B> pairWith(NetworkCodec<B> secondCodec) {
		return pairOf(this, secondCodec);
	}
}
