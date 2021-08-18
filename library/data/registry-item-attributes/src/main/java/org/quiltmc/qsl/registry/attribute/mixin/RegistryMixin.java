package org.quiltmc.qsl.registry.attribute.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryItemAttributeHolder;
import net.minecraft.util.registry.Registry;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private BuiltinRegistryItemAttributeHolder<?> qsl$builtinItemAttributeHolder;
	@Unique private BuiltinRegistryItemAttributeHolder<?> qsl$itemAttributeHolder;

	@Override
	public BuiltinRegistryItemAttributeHolder<?> qsl$getBuiltinItemAttributeHolder() {
		return qsl$builtinItemAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinItemAttributeHolder(BuiltinRegistryItemAttributeHolder<?> holder) {
		this.qsl$builtinItemAttributeHolder = holder;
	}

	@Override
	public BuiltinRegistryItemAttributeHolder<?> qsl$getItemAttributeHolder() {
		return qsl$itemAttributeHolder;
	}

	@Override
	public void qsl$setItemAttributeHolder(BuiltinRegistryItemAttributeHolder<?> holder) {
		this.qsl$itemAttributeHolder = holder;
	}
}
