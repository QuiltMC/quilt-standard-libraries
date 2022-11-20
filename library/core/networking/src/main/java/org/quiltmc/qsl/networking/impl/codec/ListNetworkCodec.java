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

package org.quiltmc.qsl.networking.impl.codec;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class ListNetworkCodec<A> implements CollectionNetworkCodec<A, List<A>> {
	private final NetworkCodec<A> entryCodec;
	private final IntFunction<? extends List<A>> listFactory;

	public ListNetworkCodec(NetworkCodec<A> entryCodec, IntFunction<? extends List<A>> listFactory) {
		this.entryCodec = entryCodec;
		this.listFactory = listFactory;
	}

	@Override
	public List<A> decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		List<A> list = this.listFactory.apply(size);

		for (int i = 0; i < size; i++) {
			list.add(this.entryCodec.decode(buf));
		}

		return list;
	}

	@Override
	public void encode(PacketByteBuf buf, List<A> data) {
		buf.writeVarInt(data.size());

		for (A entry : data) {
			this.entryCodec.encode(buf, entry);
		}
	}

	@Override
	public String toString() {
		return "ListNetworkCodec[%s]".formatted(this.entryCodec);
	}

	@Override
	public NetworkCodec<A> getEntryCodec() {
		return this.entryCodec;
	}

	@Override
	public void forEach(PacketByteBuf buf, Consumer<? super A> action) {
		int size = buf.readVarInt();

		for (int i = 0; i < size; i++) {
			action.accept(this.entryCodec.decode(buf));
		}
	}
}
