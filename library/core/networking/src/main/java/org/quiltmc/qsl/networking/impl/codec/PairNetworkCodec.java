package org.quiltmc.qsl.networking.impl.codec;

import oshi.util.tuples.Pair;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class PairNetworkCodec<A, B> implements NetworkCodec<Pair<A, B>> {
	private final NetworkCodec<A> aCodec;
	private final NetworkCodec<B> bCodec;

	public PairNetworkCodec(NetworkCodec<A> aCodec, NetworkCodec<B> bCodec) {
		this.aCodec = aCodec;
		this.bCodec = bCodec;
	}

	@Override
	public Pair<A, B> decode(PacketByteBuf buf) {
		return new Pair<>(this.aCodec.decode(buf), this.bCodec.decode(buf));
	}

	public A decodeFirst(PacketByteBuf buf) {
		return this.aCodec.decode(buf);
	}

	public B decodeSecond(PacketByteBuf buf) {
		return this.bCodec.decode(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, Pair<A, B> data) {
		this.aCodec.encode(buf, data.getA());
		this.bCodec.encode(buf, data.getB());
	}

	public void encodeFirstAndSecond(PacketByteBuf buf, A a, B b) {
		this.aCodec.encode(buf, a);
		this.bCodec.encode(buf, b);
	}

	public MapNetworkCodec.EntryCodec<A, B> intoEntry() {
		return this.aCodec.zip(this.bCodec);
	}

	@Override
	public String toString() {
		return "PairNetworkCodec{%s, %s}".formatted(this.aCodec, this.bCodec);
	}
}
