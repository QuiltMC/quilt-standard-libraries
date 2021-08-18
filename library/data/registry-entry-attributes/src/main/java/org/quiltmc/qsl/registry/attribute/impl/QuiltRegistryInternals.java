package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;

public interface QuiltRegistryInternals {
	BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder();
	void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder);

	RegistryEntryAttributeHolderImpl<?> qsl$getDataAttributeHolder();
	void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder);

	RegistryEntryAttributeHolder<?> qsl$getCombinedAttributeHolder();
}
