package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.api.event.RegistryEntryContext;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;
import org.quiltmc.qsl.registry.mixin.SimpleRegistryAccessor;

import java.util.Map;
import java.util.function.Predicate;

/**
 * The default implementation of {@link RegistryMonitor}.
 *
 * @param <V> The entry type of the monitored {@link Registry}.
 */
public class RegistryMonitorImpl<V> implements RegistryMonitor<V> {
	private final Registry<V> registry;
	private @Nullable Predicate<RegistryEntryContext<V>> filter = null;

	public RegistryMonitorImpl(Registry<V> registry) {
		this.registry = registry;
	}

	@Override
	public RegistryMonitor<V> filter(Predicate<RegistryEntryContext<V>> filter) {
		this.filter = this.filter == null ? filter : this.filter.and(filter);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void forAll(RegistryEvents.EntryAdded<V> callback) {
		var context = new MutableRegistryEntryContextImpl<>(registry);

		if (!(registry instanceof SimpleRegistryAccessor)) {
			throw new UnsupportedOperationException("Registry " + registry + " is not supported!");
		}

		for (Map.Entry<Identifier, V> entry : ((SimpleRegistryAccessor<V>) registry).quilt$getIdToEntryMap().entrySet()) {
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

	/**
	 * Tests the current filter on the specified entry context.
	 *
	 * <p>Accounts for the filter being null by treating it as always true.
	 */
	private boolean testFilter(RegistryEntryContext<V> context) {
		if (filter == null) {
			return true;
		}
		return filter.test(context);
	}
}
