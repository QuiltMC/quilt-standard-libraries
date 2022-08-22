package org.quiltmc.qsl.item.extensions.api;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Property;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(ItemUsageContext.class)
public interface QuiltItemUsageContextExtensions {
	// impls are in ItemUsageContextMixin

	default void damageStack(int amount) {}
	default void damageStack() {
		damageStack(1);
	}

	default void replaceBlock(BlockState newState) {}
	default <T extends Comparable<T>> void setBlockProperty(Property<T> property, T newValue) {}
}
