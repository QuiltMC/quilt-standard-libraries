package org.quiltmc.qsl.component.api.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.defaults.DefaultInventoryComponent;

import java.util.function.Supplier;

public interface InventoryComponent extends NbtComponent<NbtCompound>, Inventory {
	static ComponentIdentifier<InventoryComponent> ofSize(int size, Identifier id) {
		Supplier<InventoryComponent> component = () -> new DefaultInventoryComponent(size);
		return ComponentsImpl.register(id, component);
	}

	static ComponentIdentifier<InventoryComponent> of(Supplier<DefaultedList<ItemStack>> items, Identifier id) {
		Supplier<InventoryComponent> component = () -> new DefaultInventoryComponent(items);
		return ComponentsImpl.register(id, component);
	}

	DefaultedList<ItemStack> getStacks();

	@Override
	default int size() {
		return this.getStacks().size();
	}

	@Override
	default boolean isEmpty() {
		for (ItemStack stack : this.getStacks()) {
			if (!stack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	default ItemStack getStack(int slot) {
		return this.getStacks().get(slot);
	}

	@Override
	default ItemStack removeStack(int slot, int amount) {
		var result = Inventories.splitStack(this.getStacks(), slot, amount);
		this.markDirty();

		return result;
	}

	@Override
	default ItemStack removeStack(int slot) {
		var result = Inventories.removeStack(this.getStacks(), slot);
		this.markDirty();

		return result;
	}

	@Override
	default void setStack(int slot, ItemStack stack) {
		this.getStacks().set(slot, stack);
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}

		this.markDirty();
	}

	@Override
	default boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	default NbtCompound write() {
		return Inventories.writeNbt(new NbtCompound(), this.getStacks());
	}

	@Override
	default void read(NbtCompound nbt) {
		Inventories.readNbt(nbt, this.getStacks());
	}

	@Override
	default byte nbtType() {
		return NbtElement.COMPOUND_TYPE;
	}

	@Override
	void markDirty();

	@Override
	default void clear() {
		this.getStacks().clear();
		this.markDirty();
	}
}
