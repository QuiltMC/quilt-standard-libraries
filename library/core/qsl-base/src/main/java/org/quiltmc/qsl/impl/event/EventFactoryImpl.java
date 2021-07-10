package org.quiltmc.qsl.impl.event;

import java.util.function.Function;

import org.quiltmc.qsl.api.event.Event;

public final class EventFactoryImpl {
	public static <T> Event<T> createArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		return new ArrayEvent<>(type, implementation);
	}

	private EventFactoryImpl() {
	}
}
