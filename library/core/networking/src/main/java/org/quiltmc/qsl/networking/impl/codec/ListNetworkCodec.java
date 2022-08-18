package org.quiltmc.qsl.networking.impl.codec;

import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class ListNetworkCodec<A> implements NetworkCodec<List<A>> {
	private final NetworkCodec<A> entryCodec;
	private final IntFunction<? extends List<A>> listFactory;

	public ListNetworkCodec(NetworkCodec<A> entryCodec, IntFunction<? extends List<A>> listFactory) {
		this.entryCodec = entryCodec;
		this.listFactory = listFactory;
	}

	@Override
	public List<A> decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		List<A> list = this.listFactory.apply(size);

		for (int i = 0; i < size; i++) {
			list.add(this.entryCodec.decode(buf));
		}

		return list;
	}

	@Override
	public void encode(PacketByteBuf buf, List<A> data) {
		buf.writeVarInt(data.size());

		for (A entry : data) {
			this.entryCodec.encode(buf, entry);
		}
	}

	@Override
	public String toString() {
		return "ListNetworkCodec[" + this.entryCodec + "]";
	}
}
