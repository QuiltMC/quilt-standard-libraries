package org.quiltmc.qsl.networking.impl.codec;

import java.util.Collection;
import java.util.function.Consumer;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public interface CollectionNetworkCodec<A, C extends Collection<A>> extends NetworkCodec<C> {
	NetworkCodec<A> getEntryCodec();

	void forEach(PacketByteBuf buf, Consumer<? super A> action);
}
