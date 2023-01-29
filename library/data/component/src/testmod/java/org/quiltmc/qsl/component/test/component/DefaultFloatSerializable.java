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

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;

import org.quiltmc.qsl.component.api.ComponentCreationContext;
import org.quiltmc.qsl.component.api.component.NbtSerializable;

public class DefaultFloatSerializable implements NbtSerializable<NbtFloat> {
	@Nullable
	private final Runnable saveOperation;
	private float value;

	public DefaultFloatSerializable(ComponentCreationContext ops) {
		this(ops, 0);
	}

	public DefaultFloatSerializable(ComponentCreationContext ops, float initialValue) {
		this.value = initialValue;
		this.saveOperation = ops.saveOperation();
	}

	public float get() {
		return this.value;
	}

	public void set(float value) {
		this.value = value;
	}

	@Override
	public byte nbtType() {
		return NbtElement.FLOAT_TYPE;
	}

	@Override
	public void read(NbtFloat nbt) {
		this.value = nbt.floatValue();
	}

	@Override
	public NbtFloat write() {
		return NbtFloat.of(this.value);
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}
}
