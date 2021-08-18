package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public final class CombinedRegistryEntryAttributeHolderImpl<R> implements RegistryEntryAttributeHolder<R> {
	private final List<RegistryEntryAttributeHolder<R>> delegates;

	public CombinedRegistryEntryAttributeHolderImpl(List<RegistryEntryAttributeHolder<R>> delegates) {
		this.delegates = delegates;
	}

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		Optional<T> opt;
		for (var delegate : delegates) {
			if (delegate == null)
				continue;
			opt = delegate.getValue(item, attribute);
			if (opt.isPresent())
				return opt;
		}
		return Optional.empty();
	}
}
