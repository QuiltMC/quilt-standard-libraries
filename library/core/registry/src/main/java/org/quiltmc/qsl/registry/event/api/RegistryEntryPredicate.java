package org.quiltmc.qsl.registry.event.api;

import net.minecraft.util.Identifier;

@FunctionalInterface
public interface RegistryEntryPredicate<V> {
	boolean test(V entry, Identifier id, int rawId);
}
