package org.quiltmc.qsl.networking.api;

import org.quiltmc.qsl.networking.mixin.accessor.CustomPayloadC2SPacketAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.CustomPayloadS2CPacketAccessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public class CustomPayloads {
	public static <T extends CustomPayload> void registerC2SPayload(Identifier type, PacketByteBuf.Reader<T> reader) {
		CustomPayloadC2SPacketAccessor.getKnownTypes().put(type, reader);
	}

	public static <T extends CustomPayload> void registerS2CPayload(Identifier type, PacketByteBuf.Reader<T> reader) {
		CustomPayloadS2CPacketAccessor.getKnownTypes().put(type, reader);
	}
}
