package org.quiltmc.qsl.networking.api.channel;

import java.util.function.Function;

import net.minecraft.network.NetworkSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;
import org.quiltmc.qsl.networking.impl.channel.SimpleC2SNetworkChannel;

public interface NetworkChannel<T> {
	C2SNetworkChannel<Ctx> CTX = NetworkChannel.simpleC2S(
			new Identifier("test", "ctx"),
			NetworkCodec.INT.mapInt(Ctx::i, Ctx::new)
	);

	static <T> SimpleC2SNetworkChannel<T> simpleC2S(Identifier id, NetworkCodec<T> codec, Function<T, C2SNetworkChannel.Handler> transform) {
		SimpleC2SNetworkChannel<T> channel = new SimpleC2SNetworkChannel<>(id, codec, transform);
		ServerPlayNetworking.registerGlobalReceiver(channel.id(), (server, player, handler, buf, responseSender) -> {
			T data = channel.codec().decode(buf);
			channel.transform().apply(data).serverHandle(server, player, handler, responseSender);
		});

		return channel;
	}

	static <T extends C2SNetworkChannel.Handler> SimpleC2SNetworkChannel<T> simpleC2S(Identifier id, NetworkCodec<T> codec) {
		return simpleC2S(id, codec, t -> t);
	}

	Identifier getId();

	NetworkSide getSide();

	NetworkCodec<T> getCodec();

	record Ctx(int i) implements C2SNetworkChannel.Handler {

		@Override
		public void serverHandle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
				PacketSender responseSender) {
			server.sendSystemMessage(Text.of(String.valueOf(this.i)));
		}
	}
}
