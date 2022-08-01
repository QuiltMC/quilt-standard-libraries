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

package org.quiltmc.qsl.component.api.component.field;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentCreationContext;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.impl.ComponentsImpl;

public class SavedField<T> implements GenericField<T>, NbtSerializable<NbtCompound> {
	protected final Codec<T> codec;
	@Nullable
	private final Runnable saveOperation;
	protected T value;

	public SavedField(ComponentCreationContext ctx, Codec<T> codec) {
		this(ctx, codec, null);
	}

	public SavedField(ComponentCreationContext ops, Codec<T> codec, T defaultValue) {
		this.saveOperation = ops.saveOperation();
		this.codec = codec;
		this.value = defaultValue;
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public byte nbtType() {
		return NbtElement.COMPOUND_TYPE;
	}

	@Override
	public void read(NbtCompound nbt) {
		this.codec.parse(NbtOps.INSTANCE, nbt.get("Value"))
				  .result()
				  .ifPresent(this::setValue);
	}

	@Override
	public NbtCompound write() {
		var ret = new NbtCompound();
		if (this.value != null) {
			this.codec.encodeStart(NbtOps.INSTANCE, this.value)
					.resultOrPartial(ComponentsImpl.LOGGER::error)
					.ifPresent(nbtElement -> ret.put("Value", nbtElement));
		}

		return ret;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}
}
