package org.quiltmc.qsl.networking.api.channel;

import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.api.codec.NetworkCodec;
import org.quiltmc.qsl.networking.impl.channel.NetworkChannelImpl;

/**
 * A two-way network channel.
 * This kind of channel can send messages from client to server and vice-versa.
 * <p/>
 * Furthermore, this channel can be used as both an {@link S2CNetworkChannel} as well as a {@link C2SNetworkChannel},
 * for it implements the behavior of both.
 *
 * @param <T> The type of message that this channel can send
 */
public final class TwoWayNetworkChannel<T> implements S2CNetworkChannel<T>, C2SNetworkChannel<T> {
	private final Identifier id;
	private final NetworkCodec<T> codec;
	/**
	 * We can just delegate specific calls to these components, thus allowing for an easier implementation.
	 */
	private final C2SNetworkChannel<T> c2s;
	private final S2CNetworkChannel<T> s2c;

	/**
	 * Creates a new two-way network channel.
	 *
	 * @param id                 The id of this channel
	 * @param codec              The codec used by this channel
	 * @param c2sHandlerProvider the handler used on the server
	 * @param s2cHandlerProvider the handler used on the client
	 */
	public TwoWayNetworkChannel(
			Identifier id,
			NetworkCodec<T> codec,
			Supplier<Function<T, C2SNetworkChannel.Handler>> c2sHandlerProvider,
			Supplier<Function<T, S2CNetworkChannel.Handler>> s2cHandlerProvider
	) {
		this.id = id;
		this.codec = codec;
		this.c2s = NetworkChannelImpl.simpleC2S(id, codec, c2sHandlerProvider);
		this.s2c = NetworkChannelImpl.simpleS2C(id, codec, s2cHandlerProvider);
	}

	/**
	 * Gets the id of this channel.
	 *
	 * @return the id of this channel
	 */
	@Override
	public Identifier id() {
		return this.id;
	}

	/**
	 * Gets the codec used by this channel.
	 *
	 * @return the codec used by this channel
	 */
	@Override
	public NetworkCodec<T> codec() {
		return this.codec;
	}

	/**
	 * @see C2SNetworkChannel#createServerReceiver()
	 */
	@Override
	public ServerPlayNetworking.ChannelReceiver createServerReceiver() {
		return this.c2s.createServerReceiver();
	}

	/**
	 * @see S2CNetworkChannel#createClientReceiver()
	 */
	@Environment(EnvType.CLIENT)
	@Override
	public ClientPlayNetworking.ChannelReceiver createClientReceiver() {
		return this.s2c.createClientReceiver();
	}
}
