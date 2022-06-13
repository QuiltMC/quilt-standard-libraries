package org.quiltmc.qsl.component.impl.defaults;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.quiltmc.qsl.component.api.components.InventoryComponent;

import java.util.function.Supplier;

public class DefaultInventoryComponent implements InventoryComponent {
	private final DefaultedList<ItemStack> stacks;
	private final Runnable dirtyOperation = () -> {};

	public DefaultInventoryComponent(int size){
		this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
	}

	public DefaultInventoryComponent(Supplier<DefaultedList<ItemStack>> stacks){
		this.stacks = stacks.get();
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return this.stacks;
	}

	@Override
	public void markDirty() {
		this.dirtyOperation.run();
	}
}
