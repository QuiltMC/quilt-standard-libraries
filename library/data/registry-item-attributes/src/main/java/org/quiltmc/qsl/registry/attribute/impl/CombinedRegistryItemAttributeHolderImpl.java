package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttributeHolder;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public final class CombinedRegistryItemAttributeHolderImpl<R> implements RegistryItemAttributeHolder<R> {
	private final List<RegistryItemAttributeHolder<R>> delegates;

	public CombinedRegistryItemAttributeHolderImpl(List<RegistryItemAttributeHolder<R>> delegates) {
		this.delegates = delegates;
	}

	@Override
	public <T> Optional<T> getValue(R item, RegistryItemAttribute<R, T> attribute) {
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
