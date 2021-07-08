package org.quiltmc.qsl.event.impl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.quiltmc.qsl.event.api.Event;

/**
 * An event that uses an array to hold all callbacks and uses a user specified function to generate the invoker that the
 * event executes.
 *
 * <p>This implementation also uses a lock and copies the callbacks array when an invoker is being executed to ensure
 * registering a new callback does not influence any currently executing invokers.
 *
 * @param <T> the type of the invoker used to execute an event
 */
final class ArrayEvent<T> extends Event<T> {
	/**
	 * The function used to generate the implementation of the invoker. This is user specified.
	 */
	private final Function<T[], T> implementation;
	/**
	 * A lock is used to enforce thread safety. A lock was chosen to avoid avoid associating a monitor with the object
	 * in order not to influence a lock-free event implementation.
	 */
	private final Lock lock = new ReentrantLock();
	/**
	 * Registered callbacks
	 */
	private T[] callbacks;

	@SuppressWarnings("unchecked")
	ArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		Objects.requireNonNull(type, "Class specifying the type of T in the event cannot be null");
		Objects.requireNonNull(implementation, "Invoker factory cannot be null");

		this.implementation = implementation;
		this.callbacks = (T[]) Array.newInstance(type, 0);
		this.update();
	}

	@Override
	public void register(T callback) {
		Objects.requireNonNull(callback, "Callback cannot be null");

		this.lock.lock();

		try {
			// Copy the array and then expand it.
			this.callbacks = Arrays.copyOf(this.callbacks, this.callbacks.length + 1);
			this.callbacks[this.callbacks.length - 1] = callback;
			this.update();
		} finally {
			this.lock.unlock();
		}
	}

	private void update() {
		// Make a copy of the array we give to the invoker factory so entries cannot be removed from this event's
		// backing array
		this.setInvoker(this.implementation.apply(Arrays.copyOf(this.callbacks, this.callbacks.length)));
	}
}
