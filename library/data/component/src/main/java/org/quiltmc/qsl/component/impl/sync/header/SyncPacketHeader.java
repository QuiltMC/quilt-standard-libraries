package org.quiltmc.qsl.component.impl.sync.header;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.sync.ComponentHeaderRegistry;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

public record SyncPacketHeader<P extends ComponentProvider>(@NotNull NetworkCodec<P> codec) {
	public @NotNull PacketByteBuf start(@NotNull ComponentProvider provider) {
		var buf = PacketByteBufs.create();
		buf.writeInt(ComponentHeaderRegistry.HEADERS.getRawId(this));
		this.codec.encode(buf, provider);

		return buf;
	}
}
