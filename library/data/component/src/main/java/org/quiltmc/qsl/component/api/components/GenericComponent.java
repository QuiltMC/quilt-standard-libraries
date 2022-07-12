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

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;

public class GenericComponent<T, E extends NbtElement> implements NbtComponent<E> {
	protected final Codec<T> codec;
	@Nullable
	private final Runnable saveOperation;
	protected T value;

	public GenericComponent(@Nullable Runnable saveOperation, Codec<T> codec) {
		this.saveOperation = saveOperation;
		this.codec = codec;
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public byte nbtType() {
		return NbtElement.COMPOUND_TYPE;
	}

	@Override
	public void read(E nbt) {
		NbtOps.INSTANCE.withParser(this.codec).apply(nbt).result().ifPresent(t -> this.value = t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E write() { // TODO: Make sure this doesn't cause problems.
		return (E) NbtOps.INSTANCE.withEncoder(this.codec).apply(this.value).result().orElse(NbtNull.INSTANCE);
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}
}
