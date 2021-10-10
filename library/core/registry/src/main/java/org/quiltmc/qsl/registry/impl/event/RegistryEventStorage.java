package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

/**
 * A duck interface for getting registry events stored in a {@link Registry}.
 */
@ApiStatus.Internal
public interface RegistryEventStorage<V> {
	/**
	 * Gets the entry added event.
	 */
	ArrayEvent<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent();

	/**
	 * Casts a {@link Registry} to the duck interface.
	 */
	@SuppressWarnings("unchecked")
	static <W> RegistryEventStorage<W> as(Registry<W> registry) {
		return (RegistryEventStorage<W>) registry;
	}
}
