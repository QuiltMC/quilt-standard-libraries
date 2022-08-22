package org.quiltmc.qsl.networking.impl.channel;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.channel.C2SNetworkChannel;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public record SimpleC2SNetworkChannel<T>(
		Identifier id,
		NetworkCodec<T> codec,
		Supplier<Function<T, C2SNetworkChannel.Handler>> handlerProvider
) implements C2SNetworkChannel<T> {
	@Override
	public ServerPlayNetworking.ChannelReceiver createServerReceiver() {
		return (server, player, handler, buf, responseSender) -> {
			T message = this.codec.decode(buf);
			server.execute(() -> this.handlerProvider.get().apply(message).serverHandle(server, player, handler, responseSender));
		};
	}
}
