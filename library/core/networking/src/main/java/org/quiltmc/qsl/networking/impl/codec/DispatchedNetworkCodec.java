package org.quiltmc.qsl.networking.impl.codec;

import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class DispatchedNetworkCodec<T, P> implements NetworkCodec<T> {
	private final NetworkCodec<P> parent;
	private final Function<? super T, P> transformer;
	private final Function<? super P, NetworkCodec<T>> dispatch;

	public DispatchedNetworkCodec(NetworkCodec<P> parent,
			Function<? super T, P> transformer,
			Function<? super P, NetworkCodec<T>> dispatch) {
		this.parent = parent;
		this.transformer = transformer;
		this.dispatch = dispatch;
	}

	@Override
	public T decode(PacketByteBuf buf) {
		return this.dispatch.apply(this.parent.decode(buf)).decode(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, T data) {
		P pState = this.transformer.apply(data);
		this.parent.encode(buf, pState);
		this.dispatch.apply(pState).encode(buf, data);
	}

	@Override
	public String toString() {
		return "DispatchFrom[%s]".formatted(this.parent);
	}
}
