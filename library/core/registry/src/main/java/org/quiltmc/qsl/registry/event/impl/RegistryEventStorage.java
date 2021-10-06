package org.quiltmc.qsl.registry.event.impl;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.event.api.RegistryEvents;

public interface RegistryEventStorage<V> {
	ArrayEvent<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent();

	@SuppressWarnings("unchecked")
	static <W> RegistryEventStorage<W> as(Registry<W> registry) {
		return (RegistryEventStorage<W>) registry;
	}
}
