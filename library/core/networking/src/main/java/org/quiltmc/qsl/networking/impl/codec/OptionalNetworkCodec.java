package org.quiltmc.qsl.networking.impl.codec;

import java.util.Optional;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public final class OptionalNetworkCodec<A> implements NetworkCodec<Optional<A>> {
	private final NetworkCodec<A> entryCodec;

	public OptionalNetworkCodec(NetworkCodec<A> entryCodec) {
		this.entryCodec = entryCodec;
	}

	@Override
	public Optional<A> decode(PacketByteBuf buf) {
		return buf.readBoolean() ? Optional.of(this.entryCodec.decode(buf)) : Optional.empty();
	}

	@Override
	public void encode(PacketByteBuf buf, Optional<A> data) {
		buf.writeBoolean(data.isPresent());
		data.ifPresent(a -> this.entryCodec.encode(buf, a));
	}

	@Override
	public String toString() {
		return "OptionalNetworkCodec[" + this.entryCodec + "]";
	}
}
