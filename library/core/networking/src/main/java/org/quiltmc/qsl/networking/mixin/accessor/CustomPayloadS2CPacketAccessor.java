package org.quiltmc.qsl.networking.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadS2CPacket.class)
public interface CustomPayloadS2CPacketAccessor {
	@Accessor("KNOWN_TYPES")
	static Map<Identifier, PacketByteBuf.Reader<? extends CustomPayload>> getKnownTypes() {
		throw new UnsupportedOperationException("Accessor Mixin");
	}
}
