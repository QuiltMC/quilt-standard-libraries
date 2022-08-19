package org.quiltmc.qsl.networking.impl.codec;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

@SuppressWarnings("DuplicatedCode") // The duplicates are needed
public final class NetworkCodecBuilder<R> {

	public NetworkCodecBuilder() { }

	public static <T> String codecName(List<Field<T, ?>> fields) {
		return "Built[%s]".formatted(fields.stream()
			.map(tField -> tField.codec.toString())
			.reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
			.toString()
	);
}

	public NetworkCodec<R> createCodecBuilder(
			List<Field<R, ?>> fields,
			PacketByteBuf.Reader<R> initializer
	) {
		return new SimpleNetworkCodec<>(
				(buf, a) -> fields.forEach(field -> field.encodeFrom(buf, a)),
				initializer
		).named(codecName(fields));
	}

	public <B, C> Function<BiFunction<B, C, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2
	) {
		List<Field<R, ?>> fields = List.of(field1, field2);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf)
		));
	}

	// create methods with 3 to 16 fields
	public <B, C, D> Function<Function3<B, C, D, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf)
		));
	}

	public <B, C, D, E> Function<Function4<B, C, D, E, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf)
		));
	}

	public <B, C, D, E, F> Function<Function5<B, C, D, E, F, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4, field5);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf)
		));
	}

	public <B, C, D, E, F, G> Function<Function6<B, C, D, E, F, G, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4, field5, field6);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H> Function<Function7<B, C, D, E, F, G, H, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4, field5, field6, field7);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I> Function<Function8<B, C, D, E, F, G, H, I, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4, field5, field6, field7, field8);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J> Function<Function9<B, C, D, E, F, G, H, I, J, R>, NetworkCodec<R>> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9
	) {
		List<Field<R, ?>> fields = List.of(field1, field2, field3, field4, field5, field6, field7, field8, field9);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K> Function<
			Function10<B, C, D, E, F, G, H, I, J, K, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K, L> Function<
			Function11<B, C, D, E, F, G, H, I, J, K, L, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K, L, M> Function<
			Function12<B, C, D, E, F, G, H, I, J, K, L, M, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11,
			Field<R, M> field12
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf),
				field12.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K, L, M, N> Function<
			Function13<B, C, D, E, F, G, H, I, J, K, L, M, N, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11,
			Field<R, M> field12,
			Field<R, N> field13
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12,
				field13
		);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf),
				field12.decode(buf),
				field13.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K, L, M, N, O> Function<
			Function14<B, C, D, E, F, G, H, I, J, K, L, M, N, O, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11,
			Field<R, M> field12,
			Field<R, N> field13,
			Field<R, O> field14
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12,
				field13, field14
		);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf),
				field12.decode(buf),
				field13.decode(buf),
				field14.decode(buf)
		));
	}

	public <B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> Function<
			Function15<B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11,
			Field<R, M> field12,
			Field<R, N> field13,
			Field<R, O> field14,
			Field<R, P> field15
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12,
				field13, field14, field15
		);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf),
				field12.decode(buf),
				field13.decode(buf),
				field14.decode(buf),
				field15.decode(buf)
		));
	}

	// make one for 16 fields
	public <B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q> Function<
			Function16<B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R>,
			NetworkCodec<R>
		> create(
			Field<R, B> field1,
			Field<R, C> field2,
			Field<R, D> field3,
			Field<R, E> field4,
			Field<R, F> field5,
			Field<R, G> field6,
			Field<R, H> field7,
			Field<R, I> field8,
			Field<R, J> field9,
			Field<R, K> field10,
			Field<R, L> field11,
			Field<R, M> field12,
			Field<R, N> field13,
			Field<R, O> field14,
			Field<R, P> field15,
			Field<R, Q> field16
	) {
		List<Field<R, ?>> fields = List.of(
				field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12,
				field13, field14, field15, field16
		);

		return ctor -> createCodecBuilder(fields, (buf) -> ctor.apply(
				field1.decode(buf),
				field2.decode(buf),
				field3.decode(buf),
				field4.decode(buf),
				field5.decode(buf),
				field6.decode(buf),
				field7.decode(buf),
				field8.decode(buf),
				field9.decode(buf),
				field10.decode(buf),
				field11.decode(buf),
				field12.decode(buf),
				field13.decode(buf),
				field14.decode(buf),
				field15.decode(buf),
				field16.decode(buf)
		));
	}

	public static final class Field<OBJ, FT> {
		private final NetworkCodec<FT> codec;
		private final Function<? super OBJ, ? extends FT> mapper;

		public Field(NetworkCodec<FT> codec, Function<? super OBJ, ? extends FT> mapper) {
			this.codec = codec;
			this.mapper = mapper;
		}

		public void encodeFrom(PacketByteBuf buf, OBJ OBJ) {
			this.codec.encode(buf, mapper.apply(OBJ));
		}

		public FT decode(PacketByteBuf buf) {
			return this.codec.decode(buf);
		}
	}
}
