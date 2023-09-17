package org.quiltmc.qsl.networking.impl.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public record PacketByteBufPayload(Identifier id, PacketByteBuf data) implements CustomPayload {
	@Override
	public void write(PacketByteBuf byteBuf) {
		byteBuf.writeBytes(this.data);
	}
}
