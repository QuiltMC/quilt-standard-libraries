package org.quiltmc.qsl.networking.impl.codec;

import java.util.Collection;
import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class CollectionNetworkCodec<A> implements NetworkCodec<Collection<A>> {
	private final NetworkCodec<A> entryCodec;
	private final IntFunction<? extends Collection<A>> collectionFactory;

	public CollectionNetworkCodec(NetworkCodec<A> entryCodec, IntFunction<? extends Collection<A>> collectionFactory) {
		this.entryCodec = entryCodec;
		this.collectionFactory = collectionFactory;
	}

	@Override
	public Collection<A> decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		Collection<A> collection = this.collectionFactory.apply(size);

		for (int i = 0; i < size; i++) {
			collection.add(this.entryCodec.decode(buf));
		}

		return collection;
	}

	@Override
	public void encode(PacketByteBuf buf, Collection<A> data) {
		buf.writeVarInt(data.size());

		for (A entry : data) {
			this.entryCodec.encode(buf, entry);
		}
	}

	@Override
	public String toString() {
		return "CollectionNetworkCodec[" + this.entryCodec + "]";
	}
}
