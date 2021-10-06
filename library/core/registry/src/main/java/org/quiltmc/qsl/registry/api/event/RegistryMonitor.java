package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.impl.event.RegistryMonitorImpl;

public interface RegistryMonitor<V> {
	RegistryMonitor<V> filter(RegistryEntryPredicate<V> filter);

	void forAll(RegistryEvents.EntryAdded<V> callback);

	void forUpcoming(RegistryEvents.EntryAdded<V> callback);

	static <V> RegistryMonitor<V> create(Registry<V> registry) {
		return new RegistryMonitorImpl<>(registry);
	}
}
