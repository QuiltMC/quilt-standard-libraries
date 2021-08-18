package org.quiltmc.qsl.registry.attribute.impl;

public interface QuiltRegistryInternals {
	BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder();
	void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder);

	RegistryEntryAttributeHolderImpl<?> qsl$getDataAttributeHolder();
	void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder);
}
