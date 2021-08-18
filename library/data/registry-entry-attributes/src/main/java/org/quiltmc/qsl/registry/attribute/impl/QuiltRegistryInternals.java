package org.quiltmc.qsl.registry.attribute.impl;

public interface QuiltRegistryInternals {
	BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder();
	void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder);

	BuiltinRegistryEntryAttributeHolder<?> qsl$getAttributeHolder();
	void qsl$setAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder);
}
