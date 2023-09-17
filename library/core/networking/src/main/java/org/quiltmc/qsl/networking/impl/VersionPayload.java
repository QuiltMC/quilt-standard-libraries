package org.quiltmc.qsl.networking.impl;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public record VersionPayload(int[] versions) implements CustomPayload {
	public static final Identifier PACKET_ID = new Identifier("quilt", "version");

	public VersionPayload(PacketByteBuf buf) {
		this(buf.readIntArray());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeIntArray(this.versions);
	}

	@Override
	public Identifier id() {
		return PACKET_ID;
	}
}
