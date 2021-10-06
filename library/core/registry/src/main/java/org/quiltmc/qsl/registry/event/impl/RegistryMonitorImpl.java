package org.quiltmc.qsl.registry.event.impl;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.event.api.RegistryEntryPredicate;
import org.quiltmc.qsl.registry.event.api.RegistryEvents;
import org.quiltmc.qsl.registry.event.api.RegistryMonitor;

public class RegistryMonitorImpl<V> implements RegistryMonitor<V> {
	private final Registry<V> registry;
	private RegistryEntryPredicate<V> filter = (entry, id, rawId) -> true;

	public RegistryMonitorImpl(Registry<V> registry) {
		this.registry = registry;
	}

	@Override
	public RegistryMonitor<V> withFilter(RegistryEntryPredicate<V> filter) {
		this.filter = filter;
		return this;
	}

	@Override
	public void forAll(RegistryEvents.EntryAdded<V> callback) {
		registry.forEach(entry -> {
			var id = registry.getId(entry);
			var raw = registry.getRawId(entry);

			if (filter.test(entry, id, raw)) {
				callback.onAdded(registry, entry, id, raw);
			}
		});

		forUpcoming(callback);
	}

	@Override
	public void forUpcoming(RegistryEvents.EntryAdded<V> callback) {
		RegistryEvents.getEntryAddEvent(registry).register((reg, entry, id, raw) -> {
			if (filter.test(entry, id, raw)) {
				callback.onAdded(reg, entry, id, raw);
			}
		});
	}
}
