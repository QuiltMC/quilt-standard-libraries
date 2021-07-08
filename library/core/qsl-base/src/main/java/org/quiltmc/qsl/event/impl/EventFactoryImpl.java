package org.quiltmc.qsl.event.impl;

import java.util.function.Function;

import org.quiltmc.qsl.event.api.Event;

public final class EventFactoryImpl {
	public static <T> Event<T> createArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		return new ArrayEvent<>(type, implementation);
	}

	private EventFactoryImpl() {
	}
}
