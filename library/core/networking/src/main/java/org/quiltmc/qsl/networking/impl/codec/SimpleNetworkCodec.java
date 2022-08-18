package org.quiltmc.qsl.networking.impl.codec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class SimpleNetworkCodec<A> implements NetworkCodec<A> {
	private final PacketByteBuf.Reader<A> reader;
	private final PacketByteBuf.Writer<A> writer;
	@Nullable
	private final String name;

	public SimpleNetworkCodec(PacketByteBuf.Reader<A> reader, PacketByteBuf.Writer<A> writer, @Nullable String name) {
		this.reader = reader;
		this.writer = writer;
		this.name = name;
	}

	@Override
	public A decode(PacketByteBuf buf) {
		return this.reader.apply(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, A data) {
		this.writer.accept(buf, data);
	}

	@Override
	public PacketByteBuf.Reader<A> asReader() {
		return this.reader;
	}

	@Override
	public PacketByteBuf.Writer<A> asWriter() {
		return this.writer;
	}

	@Override
	public String toString() {
		return this.name != null ? "SimpleNetworkCodec[" + this.name + "]" : "SimpleNetworkCodec";
	}
}
