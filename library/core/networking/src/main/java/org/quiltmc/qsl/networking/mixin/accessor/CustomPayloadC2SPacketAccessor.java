package org.quiltmc.qsl.networking.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadC2SPacket.class)
public interface CustomPayloadC2SPacketAccessor {
	@Accessor("KNOWN_TYPES")
	static Map<Identifier, PacketByteBuf.Reader<? extends CustomPayload>> getKnownTypes() {
		throw new UnsupportedOperationException("Accessor Mixin");
	}
}
