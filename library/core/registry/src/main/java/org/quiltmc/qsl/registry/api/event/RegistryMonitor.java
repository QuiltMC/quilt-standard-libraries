package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.impl.event.RegistryMonitorImpl;

import java.util.function.Predicate;

/**
 * A higher level tool for monitoring the manipulation of Minecraft's content registries.
 *
 * @param <V>
 */
public interface RegistryMonitor<V> {
	/**
	 * A builder-like method to append a filter to the current registry monitor, for determining what entries to invoke
	 * registered callbacks for.
	 *
	 * @param filter A predicate which determines what entries to invoke callbacks for.
	 * @return The current registry monitor object, so as to allow chaining other methods in builder-like fashion.
	 */
	RegistryMonitor<V> filter(Predicate<RegistryEntryContext<V>> filter);

	/**
	 * Registers the specified callback to be invoked for <b>every entry ever</b> to be registered in the monitor's registry.
	 *
	 * <p>Entries must also match the monitor's filters.
	 *
	 * @param callback The callback to be invoked on entries.
	 */
	void forAll(RegistryEvents.EntryAdded<V> callback);

	/**
	 * Registers the specified callback to be invoked for <i>all future entries</i> to registered in the monitor's registry.
	 *
	 * <p>Entries must also match the monitor's filters.
	 *
	 * @param callback The callback to be invoked on entries.
	 */
	void forUpcoming(RegistryEvents.EntryAdded<V> callback);

	/**
	 * Creates a new {@link RegistryMonitor} to monitor the specified {@link Registry}.
	 *
	 * @param registry The {@link Registry} to monitor.
	 * @param <V>      The entry type of the {@link Registry} being monitored.
	 * @return         A new {@link RegistryMonitor} monitoring the specified {@link Registry}.
	 */
	static <V> RegistryMonitor<V> create(Registry<V> registry) {
		return new RegistryMonitorImpl<>(registry);
	}
}
