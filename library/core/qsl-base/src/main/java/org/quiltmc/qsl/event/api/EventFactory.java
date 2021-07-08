package org.quiltmc.qsl.event.api;

import java.util.function.Function;

import org.quiltmc.qsl.event.impl.EventFactoryImpl;

/**
 * Factory methods to create event instances.
 */
public final class EventFactory {
	// TODO: Document
	public static <T> Event<T> createArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		return EventFactoryImpl.createArrayEvent(type, implementation);
	}

	// TODO: Document
	public static <T> Event<T> createArrayEvent(Class<? super T> type, T emptyInvoker, Function<T[], T> implementation) {
		return EventFactoryImpl.createArrayEvent(type, callbacks -> {
			if (callbacks.length == 0) {
				return emptyInvoker;
			} else if (callbacks.length == 1) {
				return callbacks[0];
			} else {
				return implementation.apply(callbacks);
			}
		});
	}

	private EventFactory() {
	}
}
