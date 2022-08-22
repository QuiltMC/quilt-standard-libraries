package org.quiltmc.qsl.networking.impl.channel;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.channel.S2CNetworkChannel;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public record SimpleS2CNetworkChannel<T>(
		Identifier id,
		NetworkCodec<T> codec,
		Supplier<Function<T, S2CNetworkChannel.Handler>> handlerProvider
) implements S2CNetworkChannel<T> {
	@Override
	public ClientPlayNetworking.ChannelReceiver createClientReceiver() {
		return (client, handler, buf, responseSender) -> {
			T message = this.codec.decode(buf);
			client.execute(() -> this.handlerProvider.get().apply(message).clientHandle(client, handler, responseSender));
		};
	}
}
