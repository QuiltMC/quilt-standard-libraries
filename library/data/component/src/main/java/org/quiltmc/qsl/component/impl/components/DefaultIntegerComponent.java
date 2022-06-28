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

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.components.NbtComponent;

public class DefaultIntegerComponent implements IntegerComponent, NbtComponent<NbtInt> {

	private int value;
	@Nullable
	private Runnable saveOperation;

	public DefaultIntegerComponent() {
		this(0);
	}

	public DefaultIntegerComponent(int defaultValue) {
		this.value = defaultValue;
	}

	@Override
	public void increment() {
		this.value++;
	}

	@Override
	public void decrement() {
		this.value--;
	}

	@Override
	public void set(int value) {
		this.value = value;
	}

	@Override
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
		return this.saveOperation;
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}
}
