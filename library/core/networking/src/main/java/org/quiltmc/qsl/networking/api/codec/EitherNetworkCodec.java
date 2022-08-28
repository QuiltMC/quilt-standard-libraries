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

import com.mojang.datafixers.util.Either;

import net.minecraft.network.PacketByteBuf;

public class EitherNetworkCodec<A, B> implements NetworkCodec<Either<A, B>> {
	private final NetworkCodec<A> leftCodec;
	private final NetworkCodec<B> rightCodec;

	public EitherNetworkCodec(NetworkCodec<A> leftCodec, NetworkCodec<B> rightCodec) {
		this.leftCodec = leftCodec;
		this.rightCodec = rightCodec;
	}

	@Override
	public Either<A, B> decode(PacketByteBuf buf) {
		boolean isRight = buf.readBoolean();

		if (isRight) {
			return Either.right(this.rightCodec.decode(buf));
		} else {
			return Either.left(this.leftCodec.decode(buf));
		}
	}

	@Override
	public void encode(PacketByteBuf buf, Either<A, B> data) {
		data.ifLeft(a -> {
			buf.writeBoolean(false);
			this.leftCodec.encode(buf, a);
		}).ifRight(b -> {
			buf.writeBoolean(true);
			this.rightCodec.encode(buf, b);
		});
	}

	@Override
	public String toString() {
		return "EitherNetworkCodec[%s, %s]".formatted(this.leftCodec, this.rightCodec);
	}
}
