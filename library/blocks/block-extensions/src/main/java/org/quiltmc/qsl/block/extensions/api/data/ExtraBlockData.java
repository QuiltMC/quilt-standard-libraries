package org.quiltmc.qsl.block.extensions.api.data;

import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.base.api.event.ParameterInvokingEvent;
import org.quiltmc.qsl.block.extensions.impl.ExtraBlockDataImpl;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockInternals;
import net.minecraft.block.Block;
import java.util.HashMap;
import java.util.Optional;

public interface ExtraBlockData {
	/**
	 * Constructs a new builder.
	 *
	 * @return new builder
	 */
	static Builder builder() {
		return new ExtraBlockDataImpl.BuilderImpl(new HashMap<>());
	}

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
		 * Builds the collection.
		 *
		 * @return new collection
		 */
		ExtraBlockData build();
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
