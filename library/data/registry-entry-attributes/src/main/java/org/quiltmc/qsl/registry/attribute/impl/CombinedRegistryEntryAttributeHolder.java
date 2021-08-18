package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public final class CombinedRegistryEntryAttributeHolder<R> implements RegistryEntryAttributeHolder<R> {
	private final RegistryEntryAttributeHolder<R> delegate, fallback;

	public CombinedRegistryEntryAttributeHolder(RegistryEntryAttributeHolder<R> delegate, RegistryEntryAttributeHolder<R> fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		Optional<T> opt = Optional.empty();
		if (delegate != null) {
			opt = delegate.getValue(item, attribute);
			if (opt.isPresent())
				return opt;
		}
		if (fallback != null) {
			opt = fallback.getValue(item, attribute);
			if (opt.isPresent())
				return opt;
		}
		return opt;
	}
}
