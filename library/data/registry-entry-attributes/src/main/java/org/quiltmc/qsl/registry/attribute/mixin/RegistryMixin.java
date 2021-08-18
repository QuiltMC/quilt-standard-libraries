package org.quiltmc.qsl.registry.attribute.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.*;
import net.minecraft.util.registry.Registry;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private BuiltinRegistryEntryAttributeHolder<?> qsl$builtinAttributeHolder;
	@Unique private RegistryEntryAttributeHolderImpl<?> qsl$dataAttributeHolder;
	@Unique private RegistryEntryAttributeHolder<?> qsl$combinedAttributeHolder;

	@Override
	public BuiltinRegistryEntryAttributeHolder<?> qsl$getBuiltinAttributeHolder() {
		return qsl$builtinAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinAttributeHolder(BuiltinRegistryEntryAttributeHolder<?> holder) {
		this.qsl$builtinAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolderImpl<?> qsl$getDataAttributeHolder() {
		return qsl$dataAttributeHolder;
	}

	@Override
	public void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder) {
		this.qsl$dataAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolder<?> qsl$getCombinedAttributeHolder() {
		return qsl$combinedAttributeHolder;
	}

	@SuppressWarnings("unchecked")
	@Unique private void qsl$updateCombinedAttributeHolder() {
		if (qsl$builtinAttributeHolder == null && qsl$dataAttributeHolder == null) {
			qsl$combinedAttributeHolder = EmptyRegistryEntryAttributeHolder.get();
		} else {
			qsl$combinedAttributeHolder = new CombinedRegistryEntryAttributeHolder<>(
					(RegistryEntryAttributeHolder<Object>) qsl$dataAttributeHolder,
					(RegistryEntryAttributeHolder<Object>) qsl$builtinAttributeHolder);
		}
	}
}
