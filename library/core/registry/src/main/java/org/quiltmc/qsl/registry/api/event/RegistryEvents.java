package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;

public final class RegistryEvents {
	private RegistryEvents() {}

	public static <V> ArrayEvent<EntryAdded<V>> getEntryAddEvent(Registry<V> registry) {
		return RegistryEventStorage.as(registry).quilt$getEntryAddedEvent();
	}

	@FunctionalInterface
	public interface EntryAdded<V> {
		void onAdded(RegistryIterationContext<V> context);
	}
}
