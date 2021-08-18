package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import net.minecraft.util.registry.Registry;

public final class BuiltinRegistryItemAttributeSetterImpl<R> implements RegistryExtensions.ItemAttributeSetter<R> {
	private final R item;
	private final BuiltinRegistryItemAttributeHolder<R> holder;

	public BuiltinRegistryItemAttributeSetterImpl(Registry<R> registry, R item) {
		this.item = item;
		this.holder = RegistryItemAttributeHolderImpl.getBuiltin(registry);
	}

	@Override
	public <T> RegistryExtensions.ItemAttributeSetter<R> put(RegistryItemAttribute<R, T> attrib, T value) {
		holder.putValue(item, attrib, value);
		return this;
	}
}
