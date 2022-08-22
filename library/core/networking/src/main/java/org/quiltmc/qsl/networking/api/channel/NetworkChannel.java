package org.quiltmc.qsl.networking.api.channel;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;
import org.quiltmc.qsl.networking.impl.channel.NetworkChannelImpl;

/**
 * A wrapper around a NetworkCodec that allows sending, decoding and handling messages.
 *
 * @param <T> the type of message this channel can handle
 */
public interface NetworkChannel<T> {
	/**
	 * The event phase for when Network channels are registered.
	 * <p/>
	 * Registration occurs during the {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents#READY} and
	 * {@link org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents#READY} events.
	 */
	Identifier REGISTRATION_PHASE = new Identifier("quilt", "channel_registration");

	/**
	 * Create a new serverbound {@link NetworkChannel}.
	 *
	 * @param id              the id of the channel
	 * @param codec           the codec used for the messages
	 * @param handlerProvider the handler for the message
	 * @param <T>             the type of message this channel can handle
	 * @return the serverbound channel
	 */
	static <T> C2SNetworkChannel<T> createC2S(Identifier id, NetworkCodec<T> codec,
			Supplier<Function<T, C2SNetworkChannel.Handler>> handlerProvider
	) {
		return NetworkChannelImpl.simpleC2S(id, codec, handlerProvider);
	}

	/**
	 * Sorter version of {@link #createC2S(Identifier, NetworkCodec, Supplier)}.
	 * This is a specific implementation for messages that implement the handler interface.
	 * Using this a user doesn't need to specify the handler.
	 *
	 * @param id    the id of the channel
	 * @param codec the codec used for the messages
	 * @param <T>   the type of message this channel can handle
	 * @return the serverbound channel
	 * @apiNote The {@link T} type <b>must</b> implement the {@link C2SNetworkChannel.Handler} interface for this
	 * method to work.
	 */
	static <T extends C2SNetworkChannel.Handler> C2SNetworkChannel<T> createC2S(Identifier id, NetworkCodec<T> codec) {
		return NetworkChannelImpl.simpleC2S(id, codec, () -> t -> t);
	}

	/**
	 * Create a new clientbound {@link NetworkChannel}.
	 *
	 * @param id              the id of the channel
	 * @param codec           the codec used for the messages
	 * @param handlerProvider the handler for the message
	 * @param <T>             the type of message this channel can handle
	 * @return the clientbound channel
	 */
	static <T> S2CNetworkChannel<T> createS2C(Identifier id, NetworkCodec<T> codec,
			Supplier<Function<T, S2CNetworkChannel.Handler>> handlerProvider
	) {
		return NetworkChannelImpl.simpleS2C(id, codec, handlerProvider);
	}

	/**
	 * Sorter version of {@link #createS2C(Identifier, NetworkCodec, Supplier)}.
	 * This is a specific implementation for messages that implement the handler interface.
	 * Using this a user doesn't need to specify the handler.
	 *
	 * @param id    the id of the channel
	 * @param codec the codec used for the messages
	 * @param <T>   the type of message this channel can handle
	 * @return the clientbound channel
	 * @apiNote The {@link T} type <b>must</b> implement the {@link S2CNetworkChannel.Handler} interface for this
	 * method to work.
	 */
	static <T extends S2CNetworkChannel.Handler> S2CNetworkChannel<T> createS2C(Identifier id, NetworkCodec<T> codec) {
		return NetworkChannelImpl.simpleS2C(id, codec, () -> t -> t);
	}

	/**
	 * Create a new {@link TwoWayNetworkChannel}.
	 * Channels like these can send messages to both the client and the server.
	 *
	 * @param id                 the id of the channel
	 * @param codec              the codec used for the messages
	 * @param c2sHandlerProvider the handler for the message sent to the server
	 * @param s2cHandlerProvider the handler for the message sent to the client
	 * @param <T>                the type of message this channel can handle
	 * @return the two-way channel
	 */
	static <T> TwoWayNetworkChannel<T> createTwoWay(Identifier id, NetworkCodec<T> codec,
			Supplier<Function<T, C2SNetworkChannel.Handler>> c2sHandlerProvider,
			Supplier<Function<T, S2CNetworkChannel.Handler>> s2cHandlerProvider
	) {
		return new TwoWayNetworkChannel<T>(id, codec, c2sHandlerProvider, s2cHandlerProvider);
	}

	/**
	 * Sorter version of {@link #createTwoWay(Identifier, NetworkCodec, Supplier, Supplier)}.
	 * This is a specific implementation for messages that implement the handler interfaces.
	 * Using this a user doesn't need to specify the handlers for messages.
	 *
	 * @param id    the id of the channel
	 * @param codec the codec used for the messages
	 * @param <T>   the type of message this channel can handle
	 * @return the two-way channel
	 * @apiNote The {@link T} type <b>must</b> implement both the {@link C2SNetworkChannel.Handler} and
	 * the {@link S2CNetworkChannel.Handler} interfaces for this method to work.
	 */
	static <T extends C2SNetworkChannel.Handler & S2CNetworkChannel.Handler> TwoWayNetworkChannel<T> createTwoWay(
			Identifier id,
			NetworkCodec<T> codec
	) {
		return createTwoWay(id, codec, () -> t -> t, () -> t -> t);
	}

	/**
	 * Gets the id of the channel.
	 *
	 * @return the id of the channel
	 */
	Identifier id();

	/**
	 * Gets the codec of the channel
	 *
	 * @return the codec of the channel
	 */
	NetworkCodec<T> codec();
}
