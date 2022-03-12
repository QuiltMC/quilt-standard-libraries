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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.impl.DefaultValueProviderRegistryDictImpl;
import org.quiltmc.qsl.registry.dict.impl.DefaultValueRegistryDictImpl;
import org.quiltmc.qsl.registry.dict.impl.RegistryDictHolder;

/**
 * Represents a dictionary that is attached to a registry entry.
 *
 * @param <R> type of the entries in the registry
 * @param <V> attached value type
 */
@ApiStatus.NonExtendable
public interface RegistryDict<R, V> {
	/**
	 * Retrieves an already-registered dictionary.
	 *
	 * @param registry   registry dictionary is attached to
	 * @param id         dictionary identifier
	 * @param valueClass attached value class
	 * @param <R>        type of the entries in the registry
	 * @param <V>        attached value type
	 * @return the dictionary, or empty if the dictionary was not found
	 */
	static <R, V> Optional<RegistryDict<R, V>> get(Registry<R> registry, Identifier id, Class<V> valueClass) {
		return Optional.ofNullable(RegistryDictHolder.getDict(registry, id, valueClass));
	}

	/**
	 * Creates a builder for a dictionary.
	 *
	 * @param <R>        type of the entries in the registry
	 * @param <V>        attached value type
	 * @param registry   registry to attach to
	 * @param id         dictionary identifier
	 * @param valueClass attached value class
	 * @param codec      attached value codec
	 * @return a builder
	 */
	static <R, V> Builder<R, V> builder(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec) {
		return new Builder<>(registry, id, valueClass, codec);
	}

	/**
	 * Creates a builder for a dictionary using {@linkplain Codec#dispatch(Function, Function) dispatched codecs}
	 * for polymorphic types.
	 *
	 * @param registry    registry to attach to
	 * @param id          dictionary identifier
	 * @param valueClass  attached value class
	 * @param codec       type to codec mapper
	 * @param <R>         type of the entries in the registry
	 * @param <V>         attached value type
	 * @return a builder
	 */
	static <R, V extends DispatchedType> Builder<R, V> dispatchedBuilder(Registry<R> registry, Identifier id,
																		 Class<V> valueClass,
																		 Function<Identifier, Codec<? extends V>> codec) {
		return builder(registry, id, valueClass, Identifier.CODEC.dispatch(V::getType, codec));
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
		return builder(registry, id, Boolean.class, Codec.BOOL);
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
		return builder(registry, id, Integer.class, Codec.INT);
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
		return builder(registry, id, Long.class, Codec.LONG);
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
		return builder(registry, id, Float.class, Codec.FLOAT);
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
		return builder(registry, id, Double.class, Codec.DOUBLE);
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
		return builder(registry, id, String.class, Codec.STRING);
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
	 * Gets the base class of this dictionary's values.
	 *
	 * @return value class
	 */
	Class<V> valueClass();

	/**
	 * Gets the {@code Codec} used to (de)serialize this dictionary's values.
	 *
	 * @return value codec
	 */
	Codec<V> codec();

	/**
	 * Gets the side this dictionary should exist on.
	 *
	 * @return dictionary side
	 */
	Side side();

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
		 * This dictionary exists on both sides. Its values will be synchronized from server to client.
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
		private final Class<V> valueClass;
		private final Codec<V> codec;

		private Side side;
		private @Nullable V defaultValue;
		private @Nullable DefaultValueProvider<R, V> defaultValueProvider;

		private Builder(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec) {
			this.registry = registry;
			this.id = id;
			this.valueClass = valueClass;
			this.codec = codec;
			side = Side.BOTH;

			if (RegistryDictHolder.getDict(registry, id) != null) {
				throw new IllegalStateException("Dictionary with ID '%s' is already registered for registry %s!"
						.formatted(id, registry.getKey().getValue()));
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
		 * <p>Setting this will <b>remove</b> the currently set
		 * {@linkplain #defaultValueProvider(DefaultValueProvider) default value provider}!
		 *
		 * @param defaultValue default value
		 * @return this builder
		 */
		public Builder<R, V> defaultValue(@Nullable V defaultValue) {
			this.defaultValue = defaultValue;
			this.defaultValueProvider = null;
			return this;
		}

		/**
		 * Sets the <em>default value provider</em> of this dictionary, which will be used to compute a value for a
		 * specific entry, should it be missing.
		 *
		 * <p>Note that this will be computed on both sides and the computation result will <em>not</em> be synchronized.
		 *
		 * <p>Setting this will <b>remove</b> the currently set
		 * {@linkplain #defaultValue(Object) default value}!
		 *
		 * @param defaultValueProvider function to compute otherwise-missing value
		 * @return this builder
		 */
		public Builder<R, V> defaultValueProvider(@Nullable DefaultValueProvider<R, V> defaultValueProvider) {
			this.defaultValueProvider = defaultValueProvider;
			this.defaultValue = null;
			return this;
		}

		/**
		 * Builds a new dictionary.
		 *
		 * @return new dictionary
		 */
		public RegistryDict<R, V> build() {
			RegistryDict<R, V> dict;
			if (defaultValueProvider == null) {
				dict = new DefaultValueRegistryDictImpl<>(registry, id, valueClass, codec, side, defaultValue);
			} else {
				dict = new DefaultValueProviderRegistryDictImpl<>(registry, id, valueClass, codec, side, defaultValueProvider);
			}
			RegistryDictHolder.registerDict(registry, dict);
			return dict;
		}
	}
}
