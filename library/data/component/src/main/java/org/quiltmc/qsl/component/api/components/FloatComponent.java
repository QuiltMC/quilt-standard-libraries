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
import net.minecraft.nbt.NbtFloat;
import org.quiltmc.qsl.component.api.Component;


public interface FloatComponent extends Component, NbtComponent<NbtFloat> {

	@Override
	default NbtFloat write() {
		return NbtFloat.of(this.get());
	}

	float get();

	@Override
	default void read(NbtFloat nbt) {
		this.set(nbt.floatValue());
	}

	void set(float value);

	@Override
	default byte nbtType() {
		return NbtElement.FLOAT_TYPE;
	}
}
