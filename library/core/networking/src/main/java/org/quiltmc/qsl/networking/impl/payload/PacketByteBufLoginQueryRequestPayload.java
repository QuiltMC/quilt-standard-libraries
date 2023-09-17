package org.quiltmc.qsl.networking.impl.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.payload.CustomQueryPayload;
import net.minecraft.util.Identifier;

public record PacketByteBufLoginQueryRequestPayload(Identifier id, PacketByteBuf data) implements CustomQueryPayload {
	@Override
	public void write(PacketByteBuf byteBuf) {
		byteBuf.writeBytes(this.data);
	}
}
