/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
