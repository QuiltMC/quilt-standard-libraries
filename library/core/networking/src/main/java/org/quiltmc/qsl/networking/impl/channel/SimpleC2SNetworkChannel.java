package org.quiltmc.qsl.networking.impl.channel;

import java.util.function.Function;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.channel.C2SNetworkChannel;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public record SimpleC2SNetworkChannel<T>(
		Identifier id,
		NetworkCodec<T> codec,
		Function<T, Handler> transform
) implements C2SNetworkChannel<T> {
	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public NetworkCodec<T> getCodec() {
		return this.codec;
	}
}
