package org.quiltmc.qsl.base.api.event.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;

public interface EventCallbackSource<T extends CodecAwareCallback> {
	void register(Identifier id, T listener, Identifier phase);

	void register(Identifier id, T listener);

	static <R extends CodecAwareCallback> @NotNull Codec<Pair<Identifier,R>> createDelegatingCodec(@NotNull CallbackCodecSource<R> map, @NotNull Class<R> callbackClass) {
		Codec<R> callbackCodec = Identifier.CODEC.flatXmap(
				identifier ->
						map.lookup(identifier) == null
								? DataResult.<Codec<R>>error("Unregistered "+callbackClass.getSimpleName()+" callback type: " + identifier)
								: DataResult.success(map.lookup(identifier)),
				codec -> {
					Identifier key = map.lookup(codec);
					if (key == null) {
						return DataResult.error("Unregistered "+callbackClass.getSimpleName()+" callback type: " + codec);
					}
					return DataResult.success(key);
				}
		).partialDispatch("type", callback -> {
			var codecIdentifier = callback.getCodecIdentifier();
			var codec = codecIdentifier == null ? null : map.lookup(codecIdentifier);
			if (codec == null)
				return DataResult.error("Codec not provided for callback");
			return DataResult.success(codec);
		}, DataResult::success);

		return Codec.pair(Identifier.CODEC.optionalFieldOf("phase", Event.DEFAULT_PHASE).codec(), callbackCodec);
	}
}
