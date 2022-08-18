package org.quiltmc.qsl.networking.impl.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class EnumNetworkCodec<A extends Enum<A>> implements NetworkCodec<A> {
	private final A[] values;

	public EnumNetworkCodec(A[] values) {
		this.values = values;
	}

	@Override
	public A decode(PacketByteBuf buf) {
		return this.values[buf.readVarInt()];
	}

	@Override
	public void encode(PacketByteBuf buf, A data) {
		buf.writeVarInt(data.ordinal());
	}
}
