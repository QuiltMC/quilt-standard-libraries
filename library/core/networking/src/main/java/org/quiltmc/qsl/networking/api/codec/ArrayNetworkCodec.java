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

import java.util.function.Consumer;
import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

public final class ArrayNetworkCodec<A> implements NetworkCodec<A[]> {
	private final NetworkCodec<A> entryCodec;
	private final IntFunction<? extends A[]> arrayFactory;

	public ArrayNetworkCodec(NetworkCodec<A> entryCodec, IntFunction<? extends A[]> arrayFactory) {
		this.entryCodec = entryCodec;
		this.arrayFactory = arrayFactory;
	}

	@Override
	public A[] decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		A[] array = this.arrayFactory.apply(size);

		for (int i = 0; i < size; i++) {
			array[i] = this.entryCodec.decode(buf);
		}

		return array;
	}

	@Override
	public void encode(PacketByteBuf buf, A[] data) {
		buf.writeVarInt(data.length);

		for (A entry : data) {
			this.entryCodec.encode(buf, entry);
		}
	}

	public void forEach(PacketByteBuf buf, Consumer<? super A> action) {
		int size = buf.readVarInt();

		for (int i = 0; i < size; i++) {
			action.accept(this.entryCodec.decode(buf));
		}
	}

	@Override
	public String toString() {
		return "ArrayNetworkCodec[%s]".formatted(this.entryCodec);
	}
}
