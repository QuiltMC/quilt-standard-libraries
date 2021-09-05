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

package org.quiltmc.qsl.registry.attribute.api;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents an attribute that is attached to a registry entry.
 *
 * @param <R> type of the entries in the registry
 * @param <V> attached value type
 */
@SuppressWarnings("ClassCanBeRecord")
public final class RegistryEntryAttribute<R, V> {
	/**
	 * Specifies on what side(s) this attribute should exist.
	 */
	public enum Side {
		/**
		 * This attribute is client-side only.
		 */
		CLIENT,
		/**
		 * This attribute is server-side only.
		 */
		SERVER,
		/**
		 * This attribute exists on both sides. Its value will be sent from server to client.
		 */
		BOTH;

		/**
		 * Checks if attributes of this side should load in this environment.
		 * @param isClient {@code true} if client environment, {@code false} otherwise
		 * @return if the attribute value should be loaded or not
		 */
		public boolean shouldLoad(boolean isClient) {
			return switch (this) {
				case CLIENT -> isClient;
				case SERVER -> !isClient;
				case BOTH -> true;
			};
		}
	}

	/**
	 * Builder for attributes.
	 *
	 * @param <R> type of the entries in the registry
	 * @param <V> attached value type
	 */
	public static final class Builder<R, V> {
		private final Registry<R> registry;
		private final Identifier id;
		private final Codec<V> codec;

		private Side side;
		private @Nullable V defaultValue;

		private Builder(Registry<R> registry, Identifier id, Codec<V> codec) {
			this.registry = registry;
			this.id = id;
			this.codec = codec;
			side = Side.BOTH;
		}

		/**
		 * Sets what side this attribute should exist on.
		 *
		 * @param side attribute side
		 * @return this builder
		 */
		public Builder<R, V> side(Side side) {
			this.side = side;
			return this;
		}

		/**
		 * Sets the default value of this attribute.
		 *
		 * @param defaultValue default value
		 * @return this builder
		 */
		public Builder<R, V> defaultValue(@Nullable V defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Builds a new attribute.
		 *
		 * @return new attribute
		 */
		public RegistryEntryAttribute<R, V> build() {
			var attr = new RegistryEntryAttribute<>(registry, id, side, codec, defaultValue);
			RegistryEntryAttributeHolder.registerAttribute(registry, attr);
			return attr;
		}
	}

	public static <R, V> Builder<R, V> builder(Registry<R> registry, Identifier id, Codec<V> codec) {
		return new Builder<>(registry, id, codec);
	}

	public static <R, V extends DispatchedType> Builder<R, V> dispatchedBuilder(Registry<R> registry, Identifier id,
																				Function<Identifier, Codec<? extends V>> codecGetter) {
		return builder(registry, id, Identifier.CODEC.dispatch(V::getType, codecGetter));
	}

	public static <R> Builder<R, Boolean> boolBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.BOOL);
	}

	public static <R> Builder<R, Integer> intBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.INT);
	}

	public static <R> Builder<R, Long> longBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.LONG);
	}

	public static <R> Builder<R, Float> floatBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.FLOAT);
	}

	public static <R> Builder<R, Double> doubleBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.DOUBLE);
	}

	public static <R> Builder<R, String> stringBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Codec.STRING);
	}

	private final Registry<R> registry;
	private final Identifier id;
	private final Side side;
	private final Codec<V> codec;
	private final @Nullable V defaultValue;

	private RegistryEntryAttribute(Registry<R> registry, Identifier id, Side side, Codec<V> codec, @Nullable V defaultValue) {
		this.registry = registry;
		this.id = id;
		this.side = side;
		this.codec = codec;
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the registry this attribute is tied to.
	 *
	 * @return tied registry
	 */
	public Registry<R> getRegistry() {
		return registry;
	}

	/**
	 * Gets the ID of this attribute.
	 *
	 * @return attribute ID
	 */
	public Identifier getId() {
		return id;
	}

	public Side getSide() {
		return side;
	}

	/**
	 * Gets the {@code Codec} used to (de)serialize this attribute's value.
	 *
	 * @return value codec
	 */
	public Codec<V> getCodec() {
		return codec;
	}

	/**
	 * Gets the default value of this attribute. Can be {@code null}.
	 *
	 * @return default value
	 */
	public @Nullable V getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Gets the value associated with this attribute for the specified entry.<p>
	 *
	 * If the item has no value for this attribute, the attribute's
	 * {@linkplain #getDefaultValue() default value} will be returned instead, unless it is {@code null},
	 * in which case an empty optional will be returned.
	 *
	 * @param entry registry entry
	 * @return attribute value, or empty if no value is assigned
	 */
	public Optional<V> getValue(R entry) {
		return RegistryEntryAttributeHolder.getCombined(registry).getValue(entry, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttribute<?, ?> that)) return false;
		return Objects.equals(registry.getKey(), that.registry.getKey()) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registry.getKey(), id);
	}
}
