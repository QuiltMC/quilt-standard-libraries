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

import java.util.Optional;

import net.minecraft.network.PacketByteBuf;

public final class OptionalNetworkCodec<A> implements NetworkCodec<Optional<A>> {
	private final NetworkCodec<A> entryCodec;

	public OptionalNetworkCodec(NetworkCodec<A> entryCodec) {
		this.entryCodec = entryCodec;
	}

	@Override
	public Optional<A> decode(PacketByteBuf buf) {
		return buf.readBoolean() ? Optional.of(this.entryCodec.decode(buf)) : Optional.empty();
	}

	@Override
	public void encode(PacketByteBuf buf, Optional<A> data) {
		buf.writeBoolean(data.isPresent());
		data.ifPresent(a -> this.entryCodec.encode(buf, a));
	}

	@Override
	public String toString() {
		return "OptionalNetworkCodec[" + this.entryCodec + "]";
	}
}
