package org.quiltmc.qsl.registry.attribute.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryEntryAttributeHolder;
import net.minecraft.util.registry.Registry;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private BuiltinRegistryEntryAttributeHolder<?> qsl$builtinAttributeHolder;
	@Unique private BuiltinRegistryEntryAttributeHolder<?> qsl$attributeHolder;

	@Override
	public BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder() {
		return qsl$builtinAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder) {
		this.qsl$builtinAttributeHolder = holder;
	}

	@Override
	public BuiltinRegistryEntryAttributeHolder<?> qsl$getAttributeHolder() {
		return qsl$attributeHolder;
	}

	@Override
	public void qsl$setAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder) {
		this.qsl$attributeHolder = holder;
	}
}
