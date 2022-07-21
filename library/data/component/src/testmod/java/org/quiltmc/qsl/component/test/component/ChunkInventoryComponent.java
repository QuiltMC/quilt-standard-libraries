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

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;

import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

public class ChunkInventoryComponent implements InventoryComponent, SyncedComponent {
	public static final NetworkCodec<List<ItemStack>> NETWORK_CODEC =
			NetworkCodec.list(NetworkCodec.ITEM_STACK, value -> DefaultedList.ofSize(value, ItemStack.EMPTY));

	private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private final Operations ops;

	public ChunkInventoryComponent(Component.Operations ops) {
		this.ops = ops;
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return this.stacks;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.ops.saveOperation();
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		NETWORK_CODEC.encode(buf, this.stacks);
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		List<ItemStack> receivedStacks = NETWORK_CODEC.decode(buf);
		for (int i = 0; i < receivedStacks.size(); i++) {
			this.stacks.set(i, receivedStacks.get(i));
		}
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.ops.syncOperation();
	}
}
