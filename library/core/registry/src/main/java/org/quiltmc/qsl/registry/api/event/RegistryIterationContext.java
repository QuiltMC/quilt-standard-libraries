package org.quiltmc.qsl.registry.api.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface RegistryIterationContext<V> {
	Registry<V> registry();

	V entry();

	Identifier id();

	int rawId();
}
