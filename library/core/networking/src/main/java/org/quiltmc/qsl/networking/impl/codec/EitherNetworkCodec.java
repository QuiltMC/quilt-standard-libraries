package org.quiltmc.qsl.networking.impl.codec;

import com.mojang.datafixers.util.Either;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

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
