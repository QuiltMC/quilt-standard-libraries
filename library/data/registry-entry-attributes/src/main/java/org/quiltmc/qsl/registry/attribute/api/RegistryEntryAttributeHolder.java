package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolderImpl;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Optional;

public interface RegistryEntryAttributeHolder<R> {
	static <R> RegistryEntryAttributeHolder<R> get(Registry<R> registry) {
		return RegistryEntryAttributeHolderImpl.getCombined(registry);
	}

	@SuppressWarnings("unchecked")
	static <R> RegistryEntryAttributeHolder<R> get(RegistryKey<Registry<R>> registryKey) {
		Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(registryKey.getValue());
		return get(registry);
	}

	<T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute);
}
