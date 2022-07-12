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

package org.quiltmc.qsl.component.impl.components;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.InventoryComponent;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultInventoryComponent implements InventoryComponent, SyncedComponent {
	private final DefaultedList<ItemStack> stacks;
	@Nullable
	private final Runnable saveOperation;
	@Nullable
	private final Runnable syncOperation;

	public DefaultInventoryComponent(@Nullable Runnable saveOperation, @Nullable Runnable syncOperation, int size) {
		this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
		this.saveOperation = saveOperation;
		this.syncOperation = syncOperation;
	}

	public DefaultInventoryComponent(@Nullable Runnable saveOperation, @Nullable Runnable syncOperation, Supplier<DefaultedList<ItemStack>> stacks) {
		this.syncOperation = syncOperation;
		this.stacks = stacks.get();
		this.saveOperation = saveOperation;
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
	public @Nullable Runnable getSyncOperation() {
		return this.syncOperation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(stacks);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultInventoryComponent that)) return false;
		return stacks.equals(that.stacks);
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		NetworkCodec.INVENTORY.encode(buf, this.stacks);
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		NetworkCodec.INVENTORY.decode(buf).ifJust(itemStacks -> {
			for (int i = 0; i < itemStacks.size(); i++) {
				this.stacks.set(i, itemStacks.get(i));
			}
		});
	}
}
