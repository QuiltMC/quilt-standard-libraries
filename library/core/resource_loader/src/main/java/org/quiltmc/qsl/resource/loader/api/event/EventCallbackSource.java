package org.quiltmc.qsl.resource.loader.api.event;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.impl.event.IdentifiedEventCallbackSource;

public interface EventCallbackSource<T extends CodecAwareCallback<T>> {
	void register(Identifier id, T listener, Identifier phase);

	void register(Identifier id, T listener);

	static <T extends CodecAwareCallback<T>> EventCallbackSource<T> of(
			@NotNull Identifier resourcePath,
			@NotNull CallbackCodecSource<T> codecs,
			@NotNull Class<T> callbackClass,
			@NotNull Event<T> event,
			@NotNull Function<Supplier<T[]>, T> combiner,
			@NotNull ResourceType type) {
		var source = new IdentifiedEventCallbackSource<>(resourcePath, codecs, callbackClass, event, combiner);
		ResourceLoader.get(type).registerReloader(source);
		return source;
	}
}
