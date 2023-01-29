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

package org.quiltmc.qsl.component.api.component;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public interface NbtSerializable<T extends NbtElement> {
	@SuppressWarnings("unchecked")
	static void readFrom(NbtCompound root, NbtSerializable<?> nbtSerializable, Identifier id) {
		NbtElement nbtTarget = root.get(id.toString());
		switch (nbtSerializable.nbtType()) {
			case NbtElement.BYTE_TYPE -> ((NbtSerializable<NbtByte>) nbtSerializable).read((NbtByte) nbtTarget);
			case NbtElement.SHORT_TYPE -> ((NbtSerializable<NbtShort>) nbtSerializable).read((NbtShort) nbtTarget);
			case NbtElement.INT_TYPE -> ((NbtSerializable<NbtInt>) nbtSerializable).read((NbtInt) nbtTarget);
			case NbtElement.LONG_TYPE -> ((NbtSerializable<NbtLong>) nbtSerializable).read((NbtLong) nbtTarget);
			case NbtElement.FLOAT_TYPE -> ((NbtSerializable<NbtFloat>) nbtSerializable).read((NbtFloat) nbtTarget);
			case NbtElement.DOUBLE_TYPE -> ((NbtSerializable<NbtDouble>) nbtSerializable).read((NbtDouble) nbtTarget);
			case NbtElement.BYTE_ARRAY_TYPE -> ((NbtSerializable<NbtByteArray>) nbtSerializable).read((NbtByteArray) nbtTarget);
			case NbtElement.STRING_TYPE -> ((NbtSerializable<NbtString>) nbtSerializable).read((NbtString) nbtTarget);
			case NbtElement.LIST_TYPE -> ((NbtSerializable<NbtList>) nbtSerializable).read((NbtList) nbtTarget);
			case NbtElement.COMPOUND_TYPE -> ((NbtSerializable<NbtCompound>) nbtSerializable).read((NbtCompound) nbtTarget);
			case NbtElement.INT_ARRAY_TYPE -> ((NbtSerializable<NbtIntArray>) nbtSerializable).read((NbtIntArray) nbtTarget);
			case NbtElement.LONG_ARRAY_TYPE -> ((NbtSerializable<NbtLongArray>) nbtSerializable).read((NbtLongArray) nbtTarget);
			default -> throw ErrorUtil.runtime("The nbt data type with id %D is not vanilla!", nbtSerializable.nbtType()).get();
		}
	}

	static void writeTo(NbtCompound root, NbtSerializable<?> nbtSerializable, Identifier id) {
		root.put(id.toString(), nbtSerializable.write());
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
}
