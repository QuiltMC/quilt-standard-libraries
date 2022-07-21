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

package org.quiltmc.qsl.component.test.component;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import org.quiltmc.qsl.component.api.Component;

public class DefaultInventoryComponent implements InventoryComponent {
	private final DefaultedList<ItemStack> stacks;
	@Nullable
	private final Runnable saveOperation;

	public DefaultInventoryComponent(Component.Operations ops, int size) {
		this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
		this.saveOperation = ops.saveOperation();
	}

	public DefaultInventoryComponent(Component.Operations ops, Supplier<? extends DefaultedList<ItemStack>> stacks) {
		this.stacks = stacks.get();
		this.saveOperation = ops.saveOperation();
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return this.stacks;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.stacks);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultInventoryComponent that)) return false;
		return this.stacks.equals(that.stacks);
	}
}
