package org.quiltmc.qsl.resource.loader.api.event;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

public class CallbackCodecSource<T extends CodecAwareCallback<T>> {
	private final BiMap<Identifier, Codec<? extends T>> codecs = HashBiMap.create();

	public CallbackCodecSource(T nullOperation) {
		codecs.put(new Identifier("quilt","nothing"), nullOperation.getCodec());
	}

	public CallbackCodecSource() {
	}

	public void register(Identifier id, Codec<? extends T> codec) {
		if (codecs.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate codec id: " + id);
		}
		codecs.put(id, codec);
	}

	public Codec<? extends T> lookup(Identifier id) {
		return codecs.get(id);
	}
	public Identifier lookup(Codec<? extends T> codec) {
		return codecs.inverse().get(codec);
	}
}
