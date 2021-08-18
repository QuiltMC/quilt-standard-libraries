package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.registry.Registry;

public final class BuiltinRegistryEntryAttributeSetterImpl<R> implements RegistryExtensions.AttributeSetter<R> {
	private final R item;
	private final BuiltinRegistryEntryAttributeHolder<R> holder;

	public BuiltinRegistryEntryAttributeSetterImpl(Registry<R> registry, R item) {
		this.item = item;
		this.holder = RegistryEntryAttributeHolderImpl.getBuiltin(registry);
	}

	@Override
	public <T> RegistryExtensions.AttributeSetter<R> put(RegistryEntryAttribute<R, T> attrib, T value) {
		holder.putValue(item, attrib, value);
		return this;
	}
}
