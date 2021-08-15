/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.block.extensions.impl;

import org.quiltmc.qsl.block.extensions.api.data.BlockDataKey;
import org.quiltmc.qsl.block.extensions.api.data.ExtraBlockData;
import java.util.Map;
import java.util.Optional;

public record ExtraBlockDataImpl(Map<BlockDataKey<?>, Object> values) implements ExtraBlockData {
	@Override
	public boolean contains(BlockDataKey<?> key) {
		return values.containsKey(key);
	}

	@Override
	public <T> Optional<T> get(BlockDataKey<T> key) {
		Object raw = values.get(key);
		if (raw == null)
			return Optional.ofNullable(key.defaultValue());
		if (!key.type().isInstance(raw))
			throw new IllegalStateException("Value exists in collection, but type is incompatible with key type! Possible key collision?");
		return Optional.of(key.type().cast(raw));
	}

	public record BuilderImpl(Map<BlockDataKey<?>, Object> values) implements Builder {
		@Override
		public <T> Builder put(BlockDataKey<T> key, T value) {
			values.put(key, value);
			return this;
		}

		public ExtraBlockData build() {
			return new ExtraBlockDataImpl(values);
		}
	}
}
