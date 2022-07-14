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
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public interface NbtComponent<T extends NbtElement> extends Component {
	@SuppressWarnings("unchecked")
	static void readFrom(NbtComponent<?> nbtComponent, Identifier id, NbtCompound root) {
		NbtElement nbtTarget = root.get(id.toString());
		switch (nbtComponent.nbtType()) {
			case NbtElement.BYTE_TYPE -> ((NbtComponent<NbtByte>) nbtComponent).read((NbtByte) nbtTarget);
			case NbtElement.SHORT_TYPE -> ((NbtComponent<NbtShort>) nbtComponent).read((NbtShort) nbtTarget);
			case NbtElement.INT_TYPE -> ((NbtComponent<NbtInt>) nbtComponent).read((NbtInt) nbtTarget);
			case NbtElement.LONG_TYPE -> ((NbtComponent<NbtLong>) nbtComponent).read((NbtLong) nbtTarget);
			case NbtElement.FLOAT_TYPE -> ((NbtComponent<NbtFloat>) nbtComponent).read((NbtFloat) nbtTarget);
			case NbtElement.DOUBLE_TYPE -> ((NbtComponent<NbtDouble>) nbtComponent).read((NbtDouble) nbtTarget);
			case NbtElement.BYTE_ARRAY_TYPE -> ((NbtComponent<NbtByteArray>) nbtComponent).read((NbtByteArray) nbtTarget);
			case NbtElement.STRING_TYPE -> ((NbtComponent<NbtString>) nbtComponent).read((NbtString) nbtTarget);
			case NbtElement.LIST_TYPE -> ((NbtComponent<NbtList>) nbtComponent).read((NbtList) nbtTarget);
			case NbtElement.COMPOUND_TYPE -> ((NbtComponent<NbtCompound>) nbtComponent).read((NbtCompound) nbtTarget);
			case NbtElement.INT_ARRAY_TYPE -> ((NbtComponent<NbtIntArray>) nbtComponent).read((NbtIntArray) nbtTarget);
			case NbtElement.LONG_ARRAY_TYPE -> ((NbtComponent<NbtLongArray>) nbtComponent).read((NbtLongArray) nbtTarget);
			default -> // We throw if we ever find a non-vanilla type trying to be used for component serialization!
					throw ErrorUtil.runtime(
							"The nbt data type with id %D is not vanilla!",
							nbtComponent.nbtType()
					).get();
		}
	}

	static void writeTo(NbtCompound root, NbtComponent<?> nbtComponent, Identifier id) {
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
}
