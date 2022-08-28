/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.networking.api.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public final class SimpleNetworkCodec<A> implements NetworkCodec<A> {
	private final PacketByteBuf.Reader<A> reader;
	private final PacketByteBuf.Writer<A> writer;

	public SimpleNetworkCodec(PacketByteBuf.Writer<A> writer, PacketByteBuf.Reader<A> reader) {
		this.reader = reader;
		this.writer = writer;
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
		return "SimpleNetworkCodec";
	}
}
