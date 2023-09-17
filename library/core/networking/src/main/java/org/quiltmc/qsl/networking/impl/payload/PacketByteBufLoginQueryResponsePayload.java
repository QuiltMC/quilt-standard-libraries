package org.quiltmc.qsl.networking.impl.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.payload.LoginQueryResponsePayload;
import net.minecraft.util.Identifier;

public record PacketByteBufLoginQueryResponsePayload(PacketByteBuf data) implements LoginQueryResponsePayload {
	@Override
	public void write(PacketByteBuf byteBuf) {
		byteBuf.writeBytes(this.data);
	}
}
