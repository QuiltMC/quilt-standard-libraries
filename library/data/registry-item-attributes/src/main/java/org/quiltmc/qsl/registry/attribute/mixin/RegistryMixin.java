package org.quiltmc.qsl.registry.attribute.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.RegistryItemAttributeHolderImpl;
import net.minecraft.util.registry.Registry;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private RegistryItemAttributeHolderImpl<?> qsl$itemAttributeHolder;

	@Override
	public RegistryItemAttributeHolderImpl<?> qsl$getItemAttributeHolder() {
		return qsl$itemAttributeHolder;
	}

	@Override
	public void qsl$setItemAttributeHolder(RegistryItemAttributeHolderImpl<?> holder) {
		this.qsl$itemAttributeHolder = holder;
	}
}
