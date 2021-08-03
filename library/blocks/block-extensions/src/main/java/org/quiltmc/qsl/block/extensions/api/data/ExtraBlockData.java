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

package org.quiltmc.qsl.block.extensions.api.data;

import org.quiltmc.qsl.base.api.event.ArrayEvent;
import net.minecraft.block.Block;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a key to value collection used to store extra data for {@link Block}s.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ExtraBlockData {
	private final Map<BlockDataKey<?>, Object> values;

	private ExtraBlockData(Map<BlockDataKey<?>, Object> values) {
		this.values = values;
	}

	/**
	 * Checks if this collection contains the specified key.
	 *
	 * @param key key to check
	 * @return {@code true} if key has a value in this collection, {@code false} otherwise.
	 */
	public boolean contains(BlockDataKey<?> key) {
		return values.containsKey(key);
	}

	/**
	 * Gets a key's value.
	 *
	 * @param key key
	 * @param <T> value type
	 * @return value of key, or empty if value is missing.
	 */
	public <T> Optional<T> get(BlockDataKey<T> key) {
		Object raw = values.get(key);
		if (raw == null)
			return Optional.empty();
		if (!key.type().isInstance(raw))
			throw new IllegalStateException("Value exists in collection, but type is incompatible with key type! Possible key collision?");
		return Optional.of(key.type().cast(raw));
	}

	/**
	 * A builder to construct a {@code ExtraBlockData} collection.
	 */
	public static final class Builder {
		private final Map<BlockDataKey<?>, Object> values;

		/**
		 * Creates a new builder.
		 */
		public Builder() {
			values = new HashMap<>();
		}

		/**
		 * Adds a key to value pair to the collection.
		 *
		 * @param key key
		 * @param value value of key
		 * @param <T> value type
		 * @return this builder
		 */
		public <T> Builder put(BlockDataKey<T> key, T value) {
			values.put(key, value);
			return this;
		}

		/**
		 * Builds the collection.
		 *
		 * @return new collection
		 */
		public ExtraBlockData build() {
			return new ExtraBlockData(new HashMap<>(values));
		}
	}

	/**
	 * Invoked to compute an {@code ExtraBlockData} collection for a specific block.
	 */
	public interface OnBuild {
		ArrayEvent<OnBuild> EVENT = ArrayEvent.create(OnBuild.class, callbacks -> (block, settings, builder) -> {
			for (OnBuild callback : callbacks)
				callback.append(block, settings, builder);
		});

		/**
		 * Appends key to value pairs to the specified builder.
		 *
		 * @param block block
		 * @param settings block settings
		 * @param builder collection builder
		 */
		void append(Block block, Block.Settings settings, ExtraBlockData.Builder builder);
	}
}
