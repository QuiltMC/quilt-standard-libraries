package org.quiltmc.qsl.registry.event.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.event.impl.RegistryEventStorage;

public final class RegistryEvents {
	private RegistryEvents() {}

	public static <V> ArrayEvent<EntryAdded<V>> getEntryAddEvent(Registry<V> registry) {
		return RegistryEventStorage.as(registry).quilt$getEntryAddedEvent();
	}

	@FunctionalInterface
	public interface EntryAdded<V> {
		void onAdded(Registry<V> registry, V entry, Identifier id, int rawId);
	}
}
