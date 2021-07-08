package org.quiltmc.qsl.event.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for event implementations.
 *
 * <p>An event is an object which manages a invoker instance. The invoker object is returned through the
 * {@link #invoker()} method and is used to execute an event. An implementation of the event class may implement
 * an invoker allowing multiple callbacks to be executed through the invoker. What an invoker does when executed is
 * entirely up to the implementation of {@code T} set as the invoker.
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
 * <pre><code>
 * public static final Event&lt;Runnable&gt; EXAMPLE = ...; // implementation
 *
 * public void registerEvents() {
 *     // Since runnable is a functional interface, we can use the lambda form.
 *     EXAMPLE.register(() -> {
 *         // Do something
 *     });
 *
 *     // Or we can use a method reference.
 *     EXAMPLE.register(this::runSomething);
 *
 *     // Or implement the callback onto a class.
 *     // You can also use an anonymous class here; for brevity that is not included.
 *     EXAMPLE.register(new ImplementedOntoClass());
 * }
 *
 * public void runSomething() {
 *     // Do something else
 * }
 *
 * // When implementing onto a class, the class must implement the same type as the event invoker.
 * class ImplementedOntoClass implements Runnable {
 *     public void run() {
 *         // Do something else again
 *     }
 * }
 * </code></pre>
 *
 * <h2>Example: Executing an event</h2>
 *
 * TODO: How the fuck do I explain this
 *
 * <h2>Best practices for implementations of {@link Event}</h2>
 *
 * Implementations do not need to follow all the best practices, but you should consider providing the following:
 * <ul>
 *     <li>Synchronization when registering callbacks.
 *     <ul>
 *         <li>Registration of callbacks may occur on multiple threads, so thread safety when registering callbacks may
 *         be useful.
 *         <li>When the invoker is being executed, a thread safe implementation should ensure the current invoker's
 *         state should not change. This means a newly registered invoker should only be visible for execution after
 *         any current open executions of the invoker finish.
 *     </ul>
 * </ul>
 *
 * @param <T> the type of the invoker used to execute an event
 */
public abstract class Event<T> {
	/**
	 * The invoker field.
	 *
	 * <p>This field is volatile to allow implementations of this class to use a suitable thread safety mechanism if
	 * desired.
	 *
	 * @deprecated this is only protected for the purpose of being compatible with existing fabric mods.
	 * This will most likely be made private in 1.18.
	 *
	 * <p>Event implementors: use {@link #setInvoker(Object)} instead.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.Internal // not considered public api anymore
	protected volatile T invoker;

	/**
	 * Returns the invoker instance.
	 *
	 * <p>This method is used to invoke an event and execute hidden callbacks implemented by the invoker.
	 *
	 * @return the invoker instance
	 */
	public final T invoker() {
		return this.invoker;
	}

	/**
	 * Sets the invoker instance.
	 *
	 * <p>This method is protected and final since it is only intended for implementors of the {@link Event} class to
	 * set the invoker instance.
	 *
	 * <p>Since the invoker field is volatile, the invoker field should be available immediately for use.
	 *
	 * @param invoker the invoker
	 */
	// TODO: Do I need to synchronize this?
	protected final void setInvoker(T invoker) {
		this.invoker = invoker;
	}

	/**
	 * Register a callback to the event.
	 *
	 * @param callback the callback.
	 */
	public abstract void register(T callback);
}
