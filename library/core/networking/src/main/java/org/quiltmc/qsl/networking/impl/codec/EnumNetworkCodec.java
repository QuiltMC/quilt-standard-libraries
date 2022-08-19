package org.quiltmc.qsl.networking.impl.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class EnumNetworkCodec<A extends Enum<A>> implements NetworkCodec<A> {
	private final Class<A> clazz;
	private final A[] values;

	public EnumNetworkCodec(Class<A> clazz) {
		this.clazz = clazz;
		this.values = clazz.getEnumConstants();
	}

	@Override
	public A decode(PacketByteBuf buf) {
		return this.values[buf.readVarInt()];
	}

	@Override
	public void encode(PacketByteBuf buf, A data) {
		buf.writeVarInt(data.ordinal());
	}

	@Override
	public String toString() {
		return this.clazz.getSimpleName();
	}
}
