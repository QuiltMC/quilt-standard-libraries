package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import java.util.Optional;

public final class EmptyRegistryEntryAttributeHolder<R> implements RegistryEntryAttributeHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> RegistryEntryAttributeHolder<R> get() {
		return (RegistryEntryAttributeHolder<R>) INSTANCE;
	}

	private static final RegistryEntryAttributeHolder<?> INSTANCE = new EmptyRegistryEntryAttributeHolder<>();

	private EmptyRegistryEntryAttributeHolder() { }

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		return Optional.empty();
	}
}
