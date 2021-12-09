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

package org.quiltmc.qsl.registry.dict.api;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.impl.RegistryDictHolder;
import org.quiltmc.qsl.registry.dict.impl.RegistryDictImpl;

/**
 * Represents a dictionary that is attached to a registry entry.
 *
 * @param <R> type of the entries in the registry
 * @param <V> attached value type
 */
public interface RegistryDict<R, V> {
	/**
	 * Retrieves an already-registered dictionary.
	 *
	 * @param registry registry dictionary is attached to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @param <V>      attached value type
	 * @return the dictionary, or empty if the dictionary was not found
	 */
	@SuppressWarnings({"unchecked", "RedundantCast"})
	static <R, V> Optional<RegistryDict<R, V>> get(Registry<R> registry, Identifier id) {
		return Optional.ofNullable((RegistryDict<R, V>) RegistryDictHolder.getDict(registry, id));
	}

	/**
	 * Creates a builder for a dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param codec    attached value codec
	 * @param <R>      type of the entries in the registry
	 * @param <V>      attached value type
	 * @return a builder
	 */
	static <R, V> Builder<R, V> builder(Registry<R> registry, Identifier id, Codec<V> codec) {
		return new Builder<>(registry, id, codec);
	}

	/**
	 * Creates a builder for a dictionary using dispatched codecs for polymorphic types.
	 *
	 * @param registry    registry to attach to
	 * @param id          dictionary identifier
	 * @param codecGetter codec getter
	 * @param <R>         type of the entries in the registry
	 * @param <V>         attached value type
	 * @return a builder
	 */
	static <R, V extends DispatchedType> Builder<R, V> dispatchedBuilder(Registry<R> registry, Identifier id,
																		 Function<Identifier, Codec<? extends V>> codecGetter) {
		return builder(registry, id, Identifier.CODEC.dispatch(V::getType, codecGetter));
	}

	/**
	 * Creates a builder for a boolean dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Boolean> boolBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.BOOL);
	}

	/**
	 * Creates a builder for an integer dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Integer> intBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.INT);
	}

	/**
	 * Creates a builder for a long dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Long> longBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.LONG);
	}

	/**
	 * Creates a builder for a float dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Float> floatBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.FLOAT);
	}

	/**
	 * Creates a builder for a double dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Double> doubleBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.DOUBLE);
	}

	/**
	 * Creates a builder for a string dictionary.
	 *
	 * @param registry registry to attach to
	 * @param id       dictionary identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, String> stringBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.STRING);
	}

	/**
	 * Gets the registry this dictionary is attached to.
	 *
	 * @return attached registry
	 */
	Registry<R> registry();

	/**
	 * Gets the ID of this dictionary.
	 *
	 * @return dictionary ID
	 */
	Identifier id();

	/**
	 * Gets the side this dictionary should exist on.
	 *
	 * @return dictionary side
	 */
	Side side();

	/**
	 * Gets the {@code Codec} used to (de)serialize this dictionary's values.
	 *
	 * @return value codec
	 */
	Codec<V> codec();

	/**
	 * Gets the value associated with this dictionary for the specified entry.
	 *
	 * @param entry registry entry
	 * @return dictionary value, or empty if no value is assigned
	 */
	Optional<V> getValue(R entry);

	/**
	 * Specifies on what side this dictionary should exist.
	 */
	enum Side {
		/**
		 * This dictionary is client-side only.
		 */
		CLIENT(ResourceType.CLIENT_RESOURCES),
		/**
		 * This dictionary is server-side only.
		 */
		SERVER(ResourceType.SERVER_DATA),
		/**
		 * This dictionary exists on both sides. Its value will be synchronized from server to client.
		 */
		BOTH(ResourceType.SERVER_DATA);

		private final ResourceType source;

		Side(ResourceType source) {
			this.source = source;
		}

		/**
		 * Gets the source dictionaries of this side should be loaded from.
		 *
		 * @return source to use for dictionaries of this side
		 */
		public ResourceType getSource() {
			return source;
		}

		/**
		 * Checks if dictionaries of this side should load from this source.
		 *
		 * @param source the source to check
		 * @return if the dictionary value should be loaded or not
		 */
		public boolean shouldLoad(ResourceType source) {
			return this.source == source;
		}
	}

	/**
	 * Builder for dictionaries.
	 *
	 * @param <R> type of the entries in the registry
	 * @param <V> attached value type
	 */
	final class Builder<R, V> {
		private final Registry<R> registry;
		private final Identifier id;
		private final Codec<V> codec;

		private Side side;
		private @Nullable V defaultValue;
		private @Nullable ComputeFunction<R, V> computeFunction;

		private Builder(Registry<R> registry, Identifier id, Codec<V> codec) {
			this.registry = registry;
			this.id = id;
			this.codec = codec;
			side = Side.BOTH;

			if (get(registry, id).isPresent()) {
				throw new IllegalStateException("Dictionary with ID '" + id +
						"' is already registered for registry " + registry.getKey().getValue() + "!");
			}
		}

		/**
		 * Sets what side this dictionary should exist on.
		 *
		 * @param side dictionary side
		 * @return this builder
		 */
		public Builder<R, V> side(Side side) {
			this.side = side;
			return this;
		}

		/**
		 * Sets the default value of this dictionary.
		 *
		 * @param defaultValue default value
		 * @return this builder
		 */
		public Builder<R, V> defaultValue(@Nullable V defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Sets the <em>compute function</em> of this dictionary, which will be used to compute a value for a
		 * specific entry, should it be missing.
		 *
		 * <p>Note that this will be computed on both sides and the computation result will <em>not</em> be synchronized.
		 *
		 * @param computeFunction function to compute otherwise-missing value
		 * @return this builder
		 */
		public Builder<R, V> computeFunction(@Nullable ComputeFunction<R, V> computeFunction) {
			this.computeFunction = computeFunction;
			this.defaultValue = null;
			return this;
		}

		/**
		 * Builds a new dictionary.
		 *
		 * @return new dictionary
		 */
		public RegistryDict<R, V> build() {
			var dict = new RegistryDictImpl<>(registry, id, side, codec, defaultValue, computeFunction);
			RegistryDictHolder.registerDict(registry, dict);
			return dict;
		}
	}
}
