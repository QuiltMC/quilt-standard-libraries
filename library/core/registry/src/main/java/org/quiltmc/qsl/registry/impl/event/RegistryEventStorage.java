package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

public interface RegistryEventStorage<V> {
	ArrayEvent<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent();

	@SuppressWarnings("unchecked")
	static <W> RegistryEventStorage<W> as(Registry<W> registry) {
		return (RegistryEventStorage<W>) registry;
	}
}
