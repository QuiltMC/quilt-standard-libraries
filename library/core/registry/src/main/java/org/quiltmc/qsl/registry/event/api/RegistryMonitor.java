package org.quiltmc.qsl.registry.event.api;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.event.impl.RegistryMonitorImpl;

public interface RegistryMonitor<V> {
	RegistryMonitor<V> withFilter(RegistryEntryPredicate<V> filter);

	void forAll(RegistryEvents.EntryAdded<V> callback);

	void forUpcoming(RegistryEvents.EntryAdded<V> callback);

	static <V> RegistryMonitor<V> create(Registry<V> registry) {
		return new RegistryMonitorImpl<>(registry);
	}
}
