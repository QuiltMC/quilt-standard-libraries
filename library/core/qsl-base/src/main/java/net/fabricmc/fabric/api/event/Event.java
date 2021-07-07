package net.fabricmc.fabric.api.event;

import org.jetbrains.annotations.ApiStatus;

/**
 * {@inheritDoc}
 *
 * @deprecated this exists for the purpose of being compatible with existing fabric mods and will be removed during the
 * 1.18 snapshot cycle.
 *
 * @see org.quiltmc.qsl.event.api.Event
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public abstract class Event<T> extends org.quiltmc.qsl.event.api.Event<T> {
	// The fabric event class used to have more fields and methods here. Those were moved to the Quilt event class.
	// Binary compatibility can be guaranteed with the moved fields and methods.

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void register(T listener);
}
