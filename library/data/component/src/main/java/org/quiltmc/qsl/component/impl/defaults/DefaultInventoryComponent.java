package org.quiltmc.qsl.component.impl.defaults;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.InventoryComponent;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultInventoryComponent implements InventoryComponent {

	private final DefaultedList<ItemStack> stacks;
	@Nullable
	private Runnable saveOperation;

	public DefaultInventoryComponent(int size) {
		this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
	}

	public DefaultInventoryComponent(Supplier<DefaultedList<ItemStack>> stacks) {
		this.stacks = stacks.get();
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return this.stacks;
	}

	@Override
	public void saveNeeded() {
		if (this.saveOperation != null) {
			this.saveOperation.run();
		}
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultInventoryComponent that)) return false;
		return stacks.equals(that.stacks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stacks);
	}

	@Override
	public void markDirty() {
		this.saveNeeded();
	}
}
