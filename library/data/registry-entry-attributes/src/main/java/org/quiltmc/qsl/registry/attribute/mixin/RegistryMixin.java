package org.quiltmc.qsl.registry.attribute.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolderImpl;
import net.minecraft.util.registry.Registry;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private BuiltinRegistryEntryAttributeHolder<?> qsl$builtinAttributeHolder;
	@Unique private RegistryEntryAttributeHolderImpl<?> qsl$dataAttributeHolder;

	@Override
	public BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder() {
		return qsl$builtinAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder) {
		this.qsl$builtinAttributeHolder = holder;
	}

	@Override
	public RegistryEntryAttributeHolderImpl<?> qsl$getDataAttributeHolder() {
		return qsl$dataAttributeHolder;
	}

	@Override
	public void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder) {
		this.qsl$dataAttributeHolder = holder;
	}
}
