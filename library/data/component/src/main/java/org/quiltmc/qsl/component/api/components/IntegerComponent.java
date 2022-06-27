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

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

public interface IntegerComponent extends NbtComponent<NbtInt> {

	void increment();

	void decrement();

	default byte nbtType() {
		return NbtElement.INT_TYPE;
	}

	default void read(NbtInt nbt) {
		this.set(nbt.intValue());
	}

	void set(int value);

	default NbtInt write() {
		return NbtInt.of(this.get());
	}

	int get();
}
