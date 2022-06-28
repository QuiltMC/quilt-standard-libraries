/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.api.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.collection.DefaultedList;

public interface InventoryComponent extends NbtComponent<NbtCompound>, Inventory {

	@Override
	default int size() {
		return this.getStacks().size();
	}

	DefaultedList<ItemStack> getStacks();

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
		return Inventories.splitStack(this.getStacks(), slot, amount);
	}

	@Override
	default ItemStack removeStack(int slot) {
		return Inventories.removeStack(this.getStacks(), slot);
	}

	@Override
	default void setStack(int slot, ItemStack stack) {
		this.getStacks().set(slot, stack);
		ItemStack slotStack = this.getStack(slot);
		if (slotStack.getCount() > this.getMaxCountPerStack()) {
			slotStack.setCount(this.getMaxCountPerStack());
		}
	}

	@Override
	default void markDirty() {
		this.save();
	}

	@Override
	default boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	default byte nbtType() {
		return NbtElement.COMPOUND_TYPE;
	}

	@Override
	default void read(NbtCompound nbt) {
		Inventories.readNbt(nbt, this.getStacks());
	}

	@Override
	default NbtCompound write() {
		return Inventories.writeNbt(new NbtCompound(), this.getStacks());
	}

	@Override
	default void clear() {
		this.getStacks().clear();
	}
}
