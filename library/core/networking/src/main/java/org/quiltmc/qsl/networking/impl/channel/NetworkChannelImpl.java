package org.quiltmc.qsl.networking.impl.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.channel.C2SNetworkChannel;
import org.quiltmc.qsl.networking.api.channel.S2CNetworkChannel;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

@ApiStatus.Internal
public final class NetworkChannelImpl {
	private static final List<C2SNetworkChannel<?>> C2S = new ArrayList<>();
	private static final List<S2CNetworkChannel<?>> S2C = new ArrayList<>();

	public static <T> C2SNetworkChannel<T> simpleC2S(Identifier id, NetworkCodec<T> codec, Supplier<Function<T, C2SNetworkChannel.Handler>> handlerProvider) {
		Supplier<Function<T, C2SNetworkChannel.Handler>> memoizedHandler = Suppliers.memoize(handlerProvider::get);
		SimpleC2SNetworkChannel<T> channel = new SimpleC2SNetworkChannel<>(id, codec, memoizedHandler);
		C2S.add(channel);
		return channel;
	}

	public static <T> S2CNetworkChannel<T> simpleS2C(Identifier id, NetworkCodec<T> codec, Supplier<Function<T, S2CNetworkChannel.Handler>> handlerProvider) {
		Supplier<Function<T, S2CNetworkChannel.Handler>> memoizedHandler = Suppliers.memoize(handlerProvider::get);
		S2CNetworkChannel<T> channel = new SimpleS2CNetworkChannel<>(id, codec, memoizedHandler);
		S2C.add(channel);
		return channel;
	}

	// TODO: Register these events!
	public static void onServerReady(MinecraftServer ignored) {
		C2S.forEach(channel -> ServerPlayNetworking.registerGlobalReceiver(channel.id(), channel.createServerReceiver()));
	}

	@Environment(EnvType.CLIENT)
	public static void onClientReady(MinecraftClient ignored) {
		S2C.forEach(channel -> ClientPlayNetworking.registerGlobalReceiver(channel.id(), channel.createClientReceiver()));
	}
}
