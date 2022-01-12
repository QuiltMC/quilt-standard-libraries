package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;

/**
 * Events for listening to the manipulation of Minecraft's content registries.
 *
 * <p>The events are to be used for very low-level purposes, and callbacks are only called on registry manipulations
 * occurring after the event registration. This means that mod load order can affect what is picked up by these events.
 *
 * <p>For more high-level monitoring of registries, including methods to ease the inconvenience of mod load order,
 * use {@link RegistryMonitor}.
 */
public final class RegistryEvents {
	private RegistryEvents() {}

	/**
	 * Gets the entry added event for a specific Minecraft registry.
	 *
	 * <p>The event is invoked upon the addition or assignment of an entry in the specified registry.
	 *
	 * @param registry the {@link Registry} for this event to listen for
	 * @param <V>      the entry type of the {@link Registry} to listen for
	 * @return         the entry added event for the specified registry, which can have callbacks registered to it
	 */
	public static <V> Event<EntryAdded<V>> getEntryAddEvent(Registry<V> registry) {
		return RegistryEventStorage.as(registry).quilt$getEntryAddedEvent();
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #getEntryAddEvent(Registry)}.
	 * @see #getEntryAddEvent(Registry)
	 *
	 * @param <V> the entry type of the {@link Registry} being listened for
	 */
	@FunctionalInterface
	public interface EntryAdded<V> {
		/**
		 * Called when an entry in this callback's event's {@link Registry} has an entry added or assigned.
		 *
		 * @param context an object containing information regarding the registry, entry object, and ID of the entry
		 *                being registered
		 */
		void onAdded(RegistryEntryContext<V> context);
	}
}
