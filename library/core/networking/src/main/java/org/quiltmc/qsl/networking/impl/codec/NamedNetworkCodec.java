package org.quiltmc.qsl.networking.impl.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class NamedNetworkCodec<A> implements NetworkCodec<A> {
	private final String name;
	private final NetworkCodec<A> delegate;

	public NamedNetworkCodec(String name, NetworkCodec<A> delegate) {
		this.name = name;
		this.delegate = delegate;
	}

	@Override
	public A decode(PacketByteBuf buf) {
		return this.delegate.decode(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, A data) {
		this.delegate.encode(buf, data);
	}

	@Override
	public String toString() {
		return "%s(%s)".formatted(this.name, this.delegate);
	}
}
