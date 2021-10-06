package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.Identifier;

@FunctionalInterface
public interface RegistryEntryPredicate<V> {
	boolean test(V entry, Identifier id, int rawId);

	default RegistryEntryPredicate<V> and(RegistryEntryPredicate<V> other) {
		return (entry, id, rawId) ->
				this.test(entry, id, rawId) && other.test(entry, id, rawId);
	}
}
