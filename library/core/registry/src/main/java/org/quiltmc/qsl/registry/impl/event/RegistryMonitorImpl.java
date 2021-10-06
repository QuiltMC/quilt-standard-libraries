package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.api.event.RegistryEntryPredicate;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;

public class RegistryMonitorImpl<V> implements RegistryMonitor<V> {
	private final Registry<V> registry;
	private @Nullable RegistryEntryPredicate<V> filter = null;

	public RegistryMonitorImpl(Registry<V> registry) {
		this.registry = registry;
	}

	@Override
	public RegistryMonitor<V> filter(RegistryEntryPredicate<V> filter) {
		this.filter = this.filter == null ? filter : this.filter.and(filter);
		return this;
	}

	@Override
	public void forAll(RegistryEvents.EntryAdded<V> callback) {
		registry.forEach(entry -> {
			var id = registry.getId(entry);
			var raw = registry.getRawId(entry);

			if (testFilter(entry, id, raw)) {
				callback.onAdded(registry, entry, id, raw);
			}
		});

		forUpcoming(callback);
	}

	@Override
	public void forUpcoming(RegistryEvents.EntryAdded<V> callback) {
		RegistryEvents.getEntryAddEvent(registry).register((reg, entry, id, raw) -> {
			if (testFilter(entry, id, raw)) {
				callback.onAdded(reg, entry, id, raw);
			}
		});
	}

	private boolean testFilter(V entry, Identifier id, int rawId) {
		if (filter == null) {
			return true;
		}
		return filter.test(entry, id, rawId);
	}
}
