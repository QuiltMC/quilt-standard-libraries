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
import org.quiltmc.qsl.base.api.event.ParameterInvokingEvent;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockInternals;
import net.minecraft.block.Block;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a {@linkplain BlockDataKey key} to value collection used to store extra data for {@link Block}s.
 */
public interface ExtraBlockData {
	/**
	 * Gets the {@code ExtraBlockData} collection that is tied to the specified block.
	 *
	 * @param block block
	 * @return extra data collection
	 */
	static ExtraBlockData get(Block block) {
		return QuiltBlockInternals.computeExtraData(block);
	}

	/**
	 * Checks if this collection contains the specified key.
	 *
	 * @param key key to check
	 * @return {@code true} if key has a value in this collection, {@code false} otherwise.
	 */
	boolean contains(BlockDataKey<?> key);

	/**
	 * Gets a key's value.
	 *
	 * @param key key
	 * @param <T> value type
	 * @return value of key, or empty if value is missing.
	 */
	<T> Optional<T> get(BlockDataKey<T> key);

	/**
	 * A builder to construct a {@code ExtraBlockData} collection.
	 */
	interface Builder {
		/**
		 * Adds a key to value pair to the collection.
		 *
		 * @param key key
		 * @param value value of key
		 * @param <T> value type
		 * @return this builder
		 */
		<T> Builder put(BlockDataKey<T> key, T value);

		/**
		 * Adds a key to value pair to the collection, if a pair containing the specified key does not exist yet.
		 *
		 * @param key key
		 * @param value value of key
		 * @param <T> value type
		 * @return this builder
		 */
		<T> Builder putIfAbsent(BlockDataKey<T> key, T value);

		/**
		 * Adds a key to value pair to the collection, if a pair containing the specified key does not exist yet.<p>
		 *
		 * The supplier will only be invoked if the specified key does not have a value associated with it yet.
		 *
		 * @param key key
		 * @param supplier value supplier
		 * @param <T> value type
		 * @return this builder
		 */
		<T> Builder computeIfAbsent(BlockDataKey<T> key, Supplier<T> supplier);
	}

	/**
	 * Invoked to compute an {@code ExtraBlockData} collection for a specific block.
	 */
	interface OnBuild {
		@ParameterInvokingEvent
		ArrayEvent<OnBuild> EVENT = ArrayEvent.create(OnBuild.class, callbacks -> (block, settings, builder) -> {
			if (block instanceof OnBuild callback)
				callback.append(block, settings, builder);
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
		void append(Block block, Block.Settings settings, Builder builder);
	}
}
