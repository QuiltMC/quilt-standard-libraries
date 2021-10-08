package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.api.event.RegistryEntryPredicate;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.api.event.RegistryIterationContext;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;
import org.quiltmc.qsl.registry.mixin.SimpleRegistryAccessor;

import java.util.Map;

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
	@SuppressWarnings("unchecked")
	public void forAll(RegistryEvents.EntryAdded<V> callback) {
		var context = new MutableRegistryIterationContextImpl<>(registry);

		if (!(registry instanceof SimpleRegistryAccessor)) {
			throw new UnsupportedOperationException("Registry " + registry + " is not supported!");
		}

		for (Map.Entry<Identifier, V> entry : ((SimpleRegistryAccessor<V>) registry).getIdToEntryMap().entrySet()) {
			context.set(entry.getKey(), entry.getValue());

			if (this.testFilter(context)) {
				callback.onAdded(context);
			}
		}

		this.forUpcoming(callback);
	}

	@Override
	public void forUpcoming(RegistryEvents.EntryAdded<V> callback) {
		RegistryEvents.getEntryAddEvent(registry).register(context -> {
			if (this.testFilter(context)) {
				callback.onAdded(context);
			}
		});
	}

	private boolean testFilter(RegistryIterationContext<V> context) {
		if (filter == null) {
			return true;
		}
		return filter.test(context);
	}
}
