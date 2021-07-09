package org.quiltmc.qsl.event.api;

import java.util.function.Function;

import org.quiltmc.qsl.event.impl.EventFactoryImpl;

/**
 * Factory methods to create event instances.
 *
 * <h2>Array event implementation</h2>
 *
 * The array event implementation provided by the Quilt Standard Libraries is a thread safe, copy-on-write event that
 * generates an implementation of {@code T} through a user provided {@link Function function} to implement {@code T}
 * using an array of callbacks.
 *
 * <p>Executing an event is the same as defined in the documentation of the {@link Event} class.
 */
public final class EventFactory {
	/**
	 * Creates a new instance of an event which is internally backed by an array.
	 *
	 * <p>The event instance returned is thread safe through use of an internal lock to ensure registrations may be done
	 * asynchronously. The implementation of the event is copied when written to ensure any open executions of the event
	 * will not change the set of callbacks that are executed when a registration occurs.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> Event<T> createArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		return EventFactoryImpl.createArrayEvent(type, implementation);
	}

	/**
	 * Creates a new instance of an event which is internally backed by an array.
	 *
	 * <p>The event instance returned is thread safe through use of an internal lock to ensure registrations may be done
	 * asynchronously. The implementation of the event is copied when written to ensure any open executions of the event
	 * will not change the set of callbacks that are executed when a registration occurs.
	 *
	 * <p>This method adds a {@code fallbackImplementation} parameter which provides an implementation of the invoker
	 * when no callbacks are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise the other {@link #createArrayEvent(Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param fallbackImplementation the fallback implementation of T to use when the array event has no callback
	 * registrations
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> Event<T> createArrayEvent(Class<? super T> type, T fallbackImplementation, Function<T[], T> implementation) {
		return EventFactoryImpl.createArrayEvent(type, callbacks -> switch (callbacks.length) {
			case 0 -> fallbackImplementation;
			case 1 -> callbacks[0];
			// We can ensure the implementation may not remove elements from the backing array since the array given to
			// this method is a copy of the backing array.
			default -> implementation.apply(callbacks);
		});
	}

	private EventFactory() {
	}
}
