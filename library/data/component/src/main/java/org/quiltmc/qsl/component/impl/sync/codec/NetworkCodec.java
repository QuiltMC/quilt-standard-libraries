package org.quiltmc.qsl.component.impl.sync.codec;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record NetworkCodec<T>(@NotNull BiConsumer<T, PacketByteBuf> encoder, @NotNull Function<PacketByteBuf, T> decoder) {
	public void encode(@NotNull PacketByteBuf buf, T t) {
		this.encoder.accept(t, buf);
	}

	public Optional<T> decode(@NotNull PacketByteBuf buf) {
		return Optional.ofNullable(this.decoder.apply(buf));
	}
}
