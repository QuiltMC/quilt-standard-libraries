package org.quiltmc.qsl.event.impl;

import net.fabricmc.fabric.api.event.Event;

/**
 * An event implementation which delegates to a Quilt event implementation.
 *
 * <p>This is used internally to expose the old Fabric event classes while using the newer Quilt implementations to back
 * the event.
 *
 * @param <T> the type of the invoker used to execute an event
 */
@SuppressWarnings("deprecation") // impl detail
public final class FabricEventDelegate<T> extends Event<T> {
	private final org.quiltmc.qsl.event.api.Event<T> delegate;

	public FabricEventDelegate(org.quiltmc.qsl.event.api.Event<T> delegate) {
		this.delegate = delegate;
		this.update();
	}

	@Override
	public void register(T listener) {
		this.delegate.register(listener);
		this.update();
	}

	private void update() {
		// Since T is the same across the delegate and this class, we can just copy over the invoker.
		this.setInvoker(this.delegate.invoker());
	}
}
