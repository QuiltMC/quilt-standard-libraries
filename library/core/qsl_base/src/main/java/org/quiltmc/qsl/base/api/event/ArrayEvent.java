/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.base.api.event;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * An object which stores event callbacks in an array.
 *
 * <p>The factory methods for ArrayEvent allows the user to provide an implementation of {@code T} which is used to
 * execute the callbacks stored in this event instance. This allows a user to control how iteration works, whether an
 * event is cancelled after a specific callback is executed or to make an event
 * {@link ParameterInvokingEvent parameter invoking}.
 *
 * <p>Generally {@code T} should be a type which is a
 * <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.8">functional interface</a>
 * to allow callbacks registered to the event to be specified in a lambda, method reference form or implemented onto a
 * class. A way to ensure that an interface is a functional interface is to place a {@link FunctionalInterface}
 * annotation on the type. You can let T not be a functional interface, however it heavily complicates the process
 * of implementing an invoker and only allows callback implementations to be done by implementing an interface onto a
 * class or extending a class.
 *
 * <h2>Example: Registering callbacks</h2>
 *
 * The most common use of an event will be registering a callback which is executed by the event. To register a callback,
 * pass an instance of {@code T} into {@link #register}.
 *
 * <pre>{@code
 * // Events should use a dedicated functional interface for T rather than overloading multiple events to the same type
 * // to allow those who implement using a class to implement multiple events.
 * @FunctionalInterface
 * public interface Example {
 *     void doSomething();
 * }
 *
 * // You can also return this instance of ArrayEvent from a method, may be useful where a parameter is needed to get
 * // the right instance of Event.
 * public static final ArrayEvent<Example> EXAMPLE = ArrayEvent.create(...); // implementation
 *
 * public void registerEvents() {
 *     // Since T is a functional interface, we can use the lambda form.
 *     EXAMPLE.register(() -> {
 *         // Do something
 *     });
 *
 *     // Or we can use a method reference.
 *     EXAMPLE.register(this::runSomething);
 *
 *     // Or implement T using a class.
 *     // You can also use an anonymous class here; for brevity that is not included.
 *     EXAMPLE.register(new ImplementedOntoClass());
 * }
 *
 * public void runSomething() {
 *     // Do something else
 * }
 *
 * // When implementing onto a class, the class must implement the same type as the event invoker.
 * class ImplementedOntoClass implements Example {
 *     public void doSomething() {
 *         // Do something else again
 *     }
 * }
 * }</pre>
 *
 * <h2>Example: Executing an event</h2>
 *
 * Executing an event is done by calling a method on the event invoker. Where {@code T} is Example, executing an event
 * is done through the following:
 *
 * <pre>{@code
 * EXAMPLE.invoker().doSomething();
 * }</pre>
 *
 * @param <T> the type of the invoker used to execute an event and the type of the callback
 */
public final class ArrayEvent<T> {
	/**
	 * Creates a new instance of {@link ArrayEvent}.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> ArrayEvent<T> create(Class<? super T> type, Function<T[], T> implementation) {
		return new ArrayEvent<>(type, implementation);
	}

	/**
	 * Creates a new instance of {@link ArrayEvent}.
	 *
	 * <p>This method adds a {@code fallbackImplementation} parameter which provides an implementation of the invoker
	 * when no callbacks are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise the other {@link #create(Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param emptyImplementation the implementation of T to use when the array event has no callback registrations
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> ArrayEvent<T> create(Class<? super T> type, T emptyImplementation, Function<T[], T> implementation) {
		return create(type, callbacks -> switch (callbacks.length) {
			case 0 -> emptyImplementation;
			case 1 -> callbacks[0];
			// We can ensure the implementation may not remove elements from the backing array since the array given to
			// this method is a copy of the backing array.
			default -> implementation.apply(callbacks);
		});
	}

	/*
	 * The implementation details:
	 *
	 * ArrayEvent is a thread-safe event implementation which uses a lock and copies the backing array during
	 * registration of callbacks.
	 *
	 * Copy-on-write is used so that if the invoker is currently executing callbacks, it will not have it's current
	 * state modified during registration of other callbacks. The new callback registered will only be available on
	 * invoker instances returned after the registration completes.
	 *
	 * Notes for future maintainers:
	 *
	 * ArrayEvent should NEVER expose public constructors. Add some create methods instead.
	 */

	/**
	 * The function used to generate the implementation of the invoker to execute events.
	 */
	private final Function<T[], T> implementation;
	private final Lock lock = new ReentrantLock();
	/**
	 * The invoker field used to execute callbacks.
	 */
	private volatile T invoker;
	/**
	 * Registered callbacks
	 */
	private T[] callbacks;

	@SuppressWarnings("unchecked")
	private ArrayEvent(Class<? super T> type, Function<T[], T> implementation) {
		Objects.requireNonNull(type, "Class specifying the type of T in the event cannot be null");
		Objects.requireNonNull(implementation, "Function to generate invoker implementation for T cannot be null");

		this.implementation = implementation;
		this.callbacks = (T[]) Array.newInstance(type, 0);
		this.update();
	}

	/**
	 * Returns the invoker instance used to execute callbacks.
	 *
	 * <p>You should avoid storing the result of this method since the invoker may become invalid at any time. Use this
	 * method to obtain the invoker when you intend to execute an event.
	 *
	 * @return the invoker instance
	 */
	public final T invoker() {
		return this.invoker;
	}

	/**
	 * Register a callback to the event.
	 *
	 * @param callback the callback
	 */
	public final void register(T callback) {
		Objects.requireNonNull(callback, "Callback cannot be null");

		this.lock.lock();

		try {
			// Copy the array and expand it.
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
		this.invoker = this.implementation.apply(Arrays.copyOf(this.callbacks, this.callbacks.length));
	}
}
