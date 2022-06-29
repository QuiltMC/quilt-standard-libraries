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

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;

public interface NbtComponent<T extends NbtElement> extends Component {
	@SuppressWarnings("unchecked")
	static void readFrom(@NotNull NbtComponent<?> nbtComponent, @NotNull Identifier id, @NotNull NbtCompound root) {
		NbtElement nbtTarget = root.get(id.toString());
		switch (nbtComponent.nbtType()) {
			case NbtElement.BYTE_TYPE -> ((NbtComponent<NbtByte>) nbtComponent).read((NbtByte) nbtTarget);
			case NbtElement.SHORT_TYPE -> ((NbtComponent<NbtShort>) nbtComponent).read((NbtShort) nbtTarget);
			case NbtElement.INT_TYPE -> ((NbtComponent<NbtInt>) nbtComponent).read((NbtInt) nbtTarget);
			case NbtElement.LONG_TYPE -> ((NbtComponent<NbtLong>) nbtComponent).read((NbtLong) nbtTarget);
			case NbtElement.FLOAT_TYPE -> ((NbtComponent<NbtFloat>) nbtComponent).read((NbtFloat) nbtTarget);
			case NbtElement.DOUBLE_TYPE -> ((NbtComponent<NbtDouble>) nbtComponent).read((NbtDouble) nbtTarget);
			case NbtElement.BYTE_ARRAY_TYPE ->
					((NbtComponent<NbtByteArray>) nbtComponent).read((NbtByteArray) nbtTarget);
			case NbtElement.STRING_TYPE -> ((NbtComponent<NbtString>) nbtComponent).read((NbtString) nbtTarget);
			case NbtElement.LIST_TYPE -> ((NbtComponent<NbtList>) nbtComponent).read((NbtList) nbtTarget);
			case NbtElement.COMPOUND_TYPE -> ((NbtComponent<NbtCompound>) nbtComponent).read((NbtCompound) nbtTarget);
			case NbtElement.INT_ARRAY_TYPE -> ((NbtComponent<NbtIntArray>) nbtComponent).read((NbtIntArray) nbtTarget);
			case NbtElement.LONG_ARRAY_TYPE ->
					((NbtComponent<NbtLongArray>) nbtComponent).read((NbtLongArray) nbtTarget);
			default -> // TODO: Handle non-vanilla nbt types.
					throw new RuntimeException("The nbt data type with id %d is not handled at the moment!".formatted(nbtComponent.nbtType()));

		}
	}

	static void writeTo(@NotNull NbtCompound root, @NotNull NbtComponent<?> nbtComponent, @NotNull Identifier id) {
		root.put(id.toString(), nbtComponent.write());
	}

	byte nbtType();

	void read(T nbt);

	T write();

	default void save() {
		if (this.getSaveOperation() != null) {
			this.getSaveOperation().run();
		}
	}

	@Nullable Runnable getSaveOperation();

	void setSaveOperation(@Nullable Runnable runnable);
}
