package org.quiltmc.qsl.networking.impl;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public record RegisterPayload(int version, String phase, Set<Identifier> channels) implements CustomPayload {
	public static final Identifier PACKET_ID = new Identifier("quilt", "register");

	public static final String PLAY = "play";
	public static final String CONFIGURATION = "configuration";

	public RegisterPayload(PacketByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readString(),
				buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier)
		);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(this.version);
		buf.writeString(this.phase);
		buf.writeCollection(this.channels, PacketByteBuf::writeIdentifier);
	}

	@Override
	public Identifier id() {
		return PACKET_ID;
	}
}
