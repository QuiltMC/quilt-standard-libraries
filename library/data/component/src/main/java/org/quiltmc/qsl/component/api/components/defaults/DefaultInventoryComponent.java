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

package org.quiltmc.qsl.component.api.components.defaults;

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
