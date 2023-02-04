package org.quiltmc.qsl.base.api.event.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;

public class CodecMap<T extends CodecAware> {
	private final BiMap<Identifier, Codec<? extends T>> codecs = HashBiMap.create();

	public CodecMap(T nullOperation) {
		codecs.put(new Identifier("quilt","nothing"), Codec.unit(nullOperation));
	}

	public CodecMap() {
	}

	public static <R extends CodecAware> @NotNull Codec<Pair<Identifier,R>> createDelegatingCodecPhased(@NotNull CodecMap<R> map, @NotNull Class<R> callbackClass) {
		return Codec.pair(Identifier.CODEC.optionalFieldOf("phase", Event.DEFAULT_PHASE).codec(), createDelegatingCodec(map, callbackClass));
	}

	public static <R extends CodecAware> @NotNull Codec<R> createDelegatingCodec(@NotNull CodecMap<R> map, @NotNull Class<R> callbackClass) {
		return createDelegatingCodec(map, callbackClass.getSimpleName());
	}

	public static <R extends CodecAware> @NotNull Codec<R> createDelegatingCodec(@NotNull CodecMap<R> map, String descriptor) {
		return Identifier.CODEC.flatXmap(
				identifier ->
						map.lookup(identifier) == null
								? DataResult.<Codec<R>>error("Unregistered "+descriptor+" type: " + identifier)
								: DataResult.success(map.lookup(identifier)),
				codec -> {
					Identifier key = map.lookup(codec);
					if (key == null) {
						return DataResult.error("Unregistered "+descriptor+" type: " + codec);
					}
					return DataResult.success(key);
				}
		).partialDispatch("type", callback -> {
			var codecIdentifier = callback.getCodecIdentifier();
			var codec = codecIdentifier == null ? null : map.lookup(codecIdentifier);
			if (codec == null)
				return DataResult.error("Codec not provided for "+descriptor+": " + callback);
			return DataResult.success(codec);
		}, DataResult::success);
	}

	public void register(Identifier id, Codec<? extends T> codec) {
		if (codecs.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate codec id: " + id);
		} else if (codecs.containsValue(codec)) {
			throw new IllegalArgumentException("Duplicate codec: " + codec);
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
