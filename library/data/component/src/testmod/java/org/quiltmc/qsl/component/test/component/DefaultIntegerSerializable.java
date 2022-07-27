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

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentCreationContext;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Syncable;

public class DefaultIntegerSerializable implements NbtSerializable<NbtInt>, Syncable {
	private final ComponentCreationContext ops;
	private int value;

	public DefaultIntegerSerializable(ComponentCreationContext ops) {
		this(ops, 0);
	}

	public DefaultIntegerSerializable(ComponentCreationContext ops, int defaultValue) {
		this.value = defaultValue;
		this.ops = ops;
	}

	public void increment() {
		this.value++;
	}

	public void decrement() {
		this.value--;
	}

	public void set(int value) {
		this.value = value;
	}

	public int get() {
		return this.value;
	}

	@Override
	public byte nbtType() {
		return NbtElement.INT_TYPE;
	}

	@Override
	public void read(NbtInt nbt) {
		this.value = nbt.intValue();
	}

	@Override
	public NbtInt write() {
		return NbtInt.of(this.value);
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.ops.saveOperation();
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		buf.writeVarInt(this.value);
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		this.value = buf.readVarInt();
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.ops.syncOperation();
	}

	@Override
	public void save() {
		NbtSerializable.super.save();
		this.sync();
	}
}
