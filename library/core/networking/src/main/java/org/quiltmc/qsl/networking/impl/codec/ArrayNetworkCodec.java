package org.quiltmc.qsl.networking.impl.codec;

import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class ArrayNetworkCodec<A> implements NetworkCodec<A[]> {
	private final NetworkCodec<A> entryCodec;
	private final IntFunction<? extends A[]> arrayFactory;

	public ArrayNetworkCodec(NetworkCodec<A> entryCodec, IntFunction<? extends A[]> arrayFactory) {
		this.entryCodec = entryCodec;
		this.arrayFactory = arrayFactory;
	}

	@Override
	public A[] decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		A[] array = this.arrayFactory.apply(size);

		for (int i = 0; i < size; i++) {
			array[i] = this.entryCodec.decode(buf);
		}

		return array;
	}

	@Override
	public void encode(PacketByteBuf buf, A[] data) {
		buf.writeVarInt(data.length);

		for (A entry : data) {
			this.entryCodec.encode(buf, entry);
		}
	}

	@Override
	public String toString() {
		return "ArrayNetworkCodec[" + this.entryCodec + "]";
	}
}
