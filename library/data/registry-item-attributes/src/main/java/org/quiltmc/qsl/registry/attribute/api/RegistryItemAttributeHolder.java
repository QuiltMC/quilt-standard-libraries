package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.RegistryItemAttributeHolderImpl;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Optional;

public interface RegistryItemAttributeHolder<R> {
	static <R> RegistryItemAttributeHolder<R> get(Registry<R> registry) {
		return RegistryItemAttributeHolderImpl.get(registry);
	}

	@SuppressWarnings("unchecked")
	static <R> RegistryItemAttributeHolder<R> get(RegistryKey<Registry<R>> registryKey) {
		Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(registryKey.getValue());
		return get(registry);
	}

	<T> Optional<T> getValue(R item, RegistryItemAttribute<R, T> attribute);
}
