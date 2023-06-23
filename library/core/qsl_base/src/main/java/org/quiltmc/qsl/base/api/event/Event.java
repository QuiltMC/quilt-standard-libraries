/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.phase.PhaseData;
import org.quiltmc.qsl.base.api.phase.PhaseSorting;
import org.quiltmc.qsl.base.api.util.QuiltAssertions;
import org.quiltmc.qsl.base.impl.QuiltBaseImpl;
import org.quiltmc.qsl.base.impl.event.EventPhaseData;
import org.quiltmc.qsl.base.impl.event.EventRegistry;

/**
 * An object which stores event callbacks.
 * <p>
 * The factory methods for Event allows the user to provide an implementation of {@code T} which is used to
 * execute the callbacks stored in this event instance. This allows a user to control how iteration works, whether an
 * event is cancelled after a specific callback is executed or to make an event
 * {@link ParameterInvokingEvent parameter invoking}.
 * <p>
 * Generally {@code T} should be a type which is a
 * <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.8">functional interface</a>
 * to allow callbacks registered to the event to be specified in a lambda, method reference form or implemented onto a
 * class. A way to ensure that an interface is a functional interface is to place a {@link FunctionalInterface}
 * annotation on the type. You can let T not be a functional interface, however it heavily complicates the process
 * of implementing an invoker and only allows callback implementations to be done by implementing an interface onto a
 * class or extending a class.
 * <p>
 * An Event can have phases, each callback is attributed to a phase ({@link Event#DEFAULT_PHASE} if unspecified),
 * and each phase can have a defined ordering. Each event phase is identified by a {@link Identifier}, ordering is done
 * by explicitly stating that event phase A will run before event phase B, for example.
 * See {@link Event#addPhaseOrdering(Identifier, Identifier)} for more information.
 *
 * <h2>Example: Registering callbacks</h2>
 * <p>
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
 * // You can also return this instance of Event from a method, may be useful where a parameter is needed to get
 * // the right instance of Event.
 * public static final Event<Example> EXAMPLE = Event.create(...); // implementation
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
 * <p>
 * Executing an event is done by calling a method on the event invoker. Where {@code T} is Example, executing an event
 * is done through the following:
 *
 * <pre>{@code
 * EXAMPLE.invoker().doSomething();
 * }</pre>
 *
 * @param <T> the type of the invoker used to execute an event and the type of the callback
 */
public final class Event<T> {
	/**
	 * The identifier of the default phase.
	 * Have a look at {@link Event#createWithPhases} for an explanation of event phases.
	 */
	public static final Identifier DEFAULT_PHASE = new Identifier("quilt", "default");

	/**
	 * Creates a new instance of {@link Event}.
	 *
	 * @param type           the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param <T>            the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> @NotNull Event<T> create(@NotNull Class<? super T> type, @NotNull Function<T[], T> implementation) {
		return new Event<>(type, implementation);
	}

	/**
	 * Creates a new instance of {@link Event}.
	 * <p>
	 * This method adds a {@code emptyImplementation} parameter which provides an implementation of the invoker
	 * when no callbacks are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise the other {@link #create(Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type                the class representing the type of the invoker that is executed by the event
	 * @param emptyImplementation the implementation of T to use when the array event has no callback registrations
	 * @param implementation      a function which generates an invoker implementation using an array of callbacks
	 * @param <T>                 the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> @NotNull Event<T> create(@NotNull Class<? super T> type, @NotNull T emptyImplementation, @NotNull Function<T[], T> implementation) {
		return create(type, callbacks -> switch (callbacks.length) {
			case 0 -> emptyImplementation;
			case 1 -> callbacks[0];
			// We can ensure the implementation may not remove elements from the backing array since the array given to
			// this method is a copy of the backing array.
			default -> implementation.apply(callbacks);
		});
	}

	/**
	 * Create a new instance of {@link Event} with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 * <p>
	 * An event phase is a named group of callbacks, which may be ordered before or after other groups of callbacks.
	 * This allows some callbacks to take priority over other callbacks.
	 * Adding separate events should be considered before making use of multiple event phases.
	 * <p>
	 * Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the event is encouraged.
	 * <p>
	 * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param type           the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of callbacks
	 * @param defaultPhases  the default phases of this event, in the correct order. Must contain {@link Event#DEFAULT_PHASE}
	 * @param <T>            the type of the invoker executed by the event
	 * @return a new event instance
	 */
	public static <T> @NotNull Event<T> createWithPhases(@NotNull Class<? super T> type, @NotNull Function<T[], T> implementation,
			@NotNull Identifier... defaultPhases) {
		QuiltBaseImpl.ensureContainsDefaultPhase(defaultPhases);
		QuiltAssertions.ensureNoDuplicates(defaultPhases, id -> new IllegalArgumentException("Duplicate event phase: " + id));

		var event = create(type, implementation);

		for (int i = 1; i < defaultPhases.length; ++i) {
			event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
		}

		return event;
	}

	/**
	 * Registers the given listener of the listed events.
	 * <p>
	 * The registration of the listener will be refused if one of the listed event involves generics in its callback type,
	 * as checking for valid registration is just too expensive, please use the regular {@link #register(Object)} method
	 * for those as the Java compiler will be able to do the checks itself.
	 *
	 * @param listener the listener of events
	 * @param events   the events to listen
	 * @throws IllegalArgumentException if the listener doesn't listen one of the events to listen
	 * @see #register(Object)
	 * @see #register(Identifier, Object)
	 */
	public static void listenAll(@NotNull Object listener, @NotNull Event<?>... events) {
		if (events.length == 0) {
			throw new IllegalArgumentException("Tried to register a listener for an empty event list.");
		}

		EventRegistry.listenAll(listener, events);
	}

	/**
	 * The function used to generate the implementation of the invoker to execute events.
	 */
	private final Class<? super T> type;
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
	/**
	 * Registered event phases.
	 */
	private final Map<Identifier, EventPhaseData<T>> phases = new LinkedHashMap<>();
	/**
	 * Phases sorted in the correct dependency order.
	 */
	private final List<EventPhaseData<T>> sortedPhases = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private Event(Class<? super T> type, Function<T[], T> implementation) {
		Objects.requireNonNull(type, "Class specifying the type of T in the event cannot be null");
		Objects.requireNonNull(implementation, "Function to generate invoker implementation for T cannot be null");

		this.type = type;
		this.implementation = implementation;
		this.callbacks = (T[]) Array.newInstance(type, 0);
		this.update();

		EventRegistry.register(this);
	}

	/**
	 * {@return the class of the type of the invoker used to execute an event and the class of the type of the callback}
	 */
	@Contract(pure = true)
	public @NotNull Class<? super T> getType() {
		return this.type;
	}

	/**
	 * Register a callback to the event.
	 *
	 * @param callback the callback
	 * @see #register(Identifier, Object)
	 */
	public void register(@NotNull T callback) {
		this.register(DEFAULT_PHASE, callback);
	}

	/**
	 * Registers a callback to a specific phase of the event.
	 *
	 * @param phaseIdentifier the phase identifier
	 * @param callback        the callback
	 */
	public void register(@NotNull Identifier phaseIdentifier, @NotNull T callback) {
		Objects.requireNonNull(phaseIdentifier, "Tried to register a callback for a null phase!");
		Objects.requireNonNull(callback, "Tried to register a null callback!");

		this.lock.lock();
		try {
			this.getOrCreatePhase(phaseIdentifier, true).addListener(callback);
			this.rebuildInvoker(this.callbacks.length + 1);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Returns the invoker instance used to execute callbacks.
	 *
	 * <p>You should avoid storing the result of this method since the invoker may become invalid at any time. Use this
	 * method to obtain the invoker when you intend to execute an event.
	 *
	 * @return the invoker instance
	 */
	@Contract(pure = true)
	public @NotNull T invoker() {
		return this.invoker;
	}

	/**
	 * Request that callbacks registered for one phase be executed before callbacks registered for another phase.
	 * Relying on the default phases supplied to {@link Event#createWithPhases} should be preferred over manually
	 * registering phase ordering dependencies.
	 * <p>
	 * Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * @param firstPhase  the identifier of the phase that should run before the other. It will be created if it didn't exist yet
	 * @param secondPhase the identifier of the phase that should run after the other. It will be created if it didn't exist yet
	 */
	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
		Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");

		if (firstPhase.equals(secondPhase)) {
			throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
		}

		synchronized (this.lock) {
			var first = this.getOrCreatePhase(firstPhase, false);
			var second = this.getOrCreatePhase(secondPhase, false);
			PhaseData.link(first, second);
			PhaseSorting.sortPhases(this.sortedPhases);
			this.rebuildInvoker(this.callbacks.length);
		}
	}

	/* Implementation */

	private EventPhaseData<T> getOrCreatePhase(Identifier id, boolean sortIfCreate) {
		var phase = this.phases.get(id);

		if (phase == null) {
			phase = new EventPhaseData<>(id, this.callbacks.getClass().getComponentType());
			this.phases.put(id, phase);
			this.sortedPhases.add(phase);

			if (sortIfCreate) {
				PhaseSorting.sortPhases(this.sortedPhases);
			}
		}

		return phase;
	}

	private void rebuildInvoker(int newLength) {
		// Rebuild handlers.
		if (this.sortedPhases.size() == 1) {
			// Special case with a single phase: use the array of the phase directly.
			this.callbacks = this.sortedPhases.get(0).getData();
		} else {
			@SuppressWarnings("unchecked")
			var newCallbacks = (T[]) Array.newInstance(this.callbacks.getClass().getComponentType(), newLength);
			int newHandlersIndex = 0;

			for (var existingPhase : this.sortedPhases) {
				int length = existingPhase.getData().length;
				System.arraycopy(existingPhase.getData(), 0, newCallbacks, newHandlersIndex, length);
				newHandlersIndex += length;
			}

			this.callbacks = newCallbacks;
		}

		// Rebuild invoker.
		this.update();
	}

	private void update() {
		// Make a copy of the array we give to the invoker factory so entries cannot be removed from this event's
		// backing array
		this.invoker = this.implementation.apply(Arrays.copyOf(this.callbacks, this.callbacks.length));
	}

	@Override
	public String toString() {
		return "Event{" +
				"type=" + this.type +
				", implementation=" + this.implementation +
				", phases=" + this.phases +
				", sortedPhases=" + this.sortedPhases +
				'}';
	}
}
