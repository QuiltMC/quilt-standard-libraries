package org.quiltmc.qsl.registry.attribute.impl;

public interface QuiltRegistryInternals {
	BuiltinRegistryItemAttributeHolder<?> qsl$getBuiltinItemAttributeHolder();
	void qsl$setBuiltinItemAttributeHolder(BuiltinRegistryItemAttributeHolder<?> holder);

	BuiltinRegistryItemAttributeHolder<?> qsl$getItemAttributeHolder();
	void qsl$setItemAttributeHolder(BuiltinRegistryItemAttributeHolder<?> holder);
}
