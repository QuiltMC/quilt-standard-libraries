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

import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;

public class DispatchedNetworkCodec<T, P> implements NetworkCodec<T> {
	private final NetworkCodec<P> parent;
	private final Function<? super T, P> transformer;
	private final Function<? super P, NetworkCodec<T>> dispatch;

	public DispatchedNetworkCodec(NetworkCodec<P> parent,
			Function<? super T, P> transformer,
			Function<? super P, NetworkCodec<T>> dispatch) {
		this.parent = parent;
		this.transformer = transformer;
		this.dispatch = dispatch;
	}

	@Override
	public T decode(PacketByteBuf buf) {
		return this.dispatch.apply(this.parent.decode(buf)).decode(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, T data) {
		P pState = this.transformer.apply(data);
		this.parent.encode(buf, pState);
		this.dispatch.apply(pState).encode(buf, data);
	}

	@Override
	public String toString() {
		return "DispatchFrom[%s]".formatted(this.parent);
	}
}
