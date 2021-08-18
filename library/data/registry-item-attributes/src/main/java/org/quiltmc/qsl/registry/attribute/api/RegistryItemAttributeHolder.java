package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.RegistryItemAttributeHolderImpl;
import net.minecraft.util.registry.Registry;
import java.util.Optional;

public interface RegistryItemAttributeHolder<R> {
	static <R> RegistryItemAttributeHolder<R> get(Registry<R> registry) {
		return RegistryItemAttributeHolderImpl.get(registry);
	}

	<T> Optional<T> getValue(R item, RegistryItemAttribute<R, T> attribute);
}
