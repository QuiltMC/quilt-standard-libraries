package org.quiltmc.qsl.networking.api;

import java.util.Objects;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.mixin.accessor.CustomPayloadC2SPacketAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.CustomPayloadS2CPacketAccessor;

/**
 * Allows for registering custom payloads for between the server and client.
 * <p>
 * Note: Registering a payload does not register a receiver and vice versa.
 */
public class CustomPayloads {
	/**
	 * Registers a common ({@link net.minecraft.network.NetworkState#CONFIGURATION} or {@link net.minecraft.network.NetworkState#PLAY}) payload that is sent from the client to the server.
	 *
	 * @param type   the type or id for the payload
	 * @param reader the deserializer for the payload
	 * @param <T>    the payload type
	 * @see ClientConfigurationNetworking#registerGlobalReceiver(Identifier, ClientConfigurationNetworking.CustomChannelReceiver)
	 * @see ClientPlayNetworking#registerGlobalReceiver(Identifier, ClientPlayNetworking.CustomChannelReceiver)
	 */
	public static <T extends CustomPayload> void registerC2SPayload(Identifier type, PacketByteBuf.Reader<T> reader) {
		Objects.requireNonNull(type, "Type cannot be null");
		Objects.requireNonNull(reader, "Reader cannot be null");

		CustomPayloadC2SPacketAccessor.getKnownTypes().put(type, reader);
	}

	/**
	 * Registers a common ({@link NetworkState#CONFIGURATION} or {@link NetworkState#PLAY}) payload that is sent from the server to the client.
	 *
	 * @param type   the type or id for the payload
	 * @param reader the deserializer for the payload
	 * @param <T>    the payload type
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(Identifier, ServerConfigurationNetworking.CustomChannelReceiver)
	 * @see ServerPlayNetworking#registerGlobalReceiver(Identifier, ServerPlayNetworking.CustomChannelReceiver)
	 */
	public static <T extends CustomPayload> void registerS2CPayload(Identifier type, PacketByteBuf.Reader<T> reader) {
		Objects.requireNonNull(type, "Type cannot be null");
		Objects.requireNonNull(reader, "Reader cannot be null");

		CustomPayloadS2CPacketAccessor.getKnownTypes().put(type, reader);
	}
}
