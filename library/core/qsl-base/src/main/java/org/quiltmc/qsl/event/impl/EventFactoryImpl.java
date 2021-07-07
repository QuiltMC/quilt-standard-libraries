package org.quiltmc.qsl.event.impl;

import java.util.function.Function;

import org.quiltmc.qsl.event.api.Event;

public final class EventFactoryImpl {
	public static <T> Event<T> createArrayEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new ArrayEvent<>(type, invokerFactory);
	}

	private EventFactoryImpl() {
	}
}
