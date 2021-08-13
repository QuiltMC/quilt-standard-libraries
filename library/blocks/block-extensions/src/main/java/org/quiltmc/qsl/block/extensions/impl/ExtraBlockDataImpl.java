package org.quiltmc.qsl.block.extensions.impl;

import org.quiltmc.qsl.block.extensions.api.data.BlockDataKey;
import org.quiltmc.qsl.block.extensions.api.data.ExtraBlockData;
import java.util.HashMap;
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
			return Optional.empty();
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

		@Override
		public ExtraBlockData build() {
			return new ExtraBlockDataImpl(new HashMap<>(values));
		}
	}
}
