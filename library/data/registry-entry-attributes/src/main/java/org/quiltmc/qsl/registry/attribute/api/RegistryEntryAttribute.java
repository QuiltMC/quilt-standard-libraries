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

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents an attribute that is attached to a registry entry. Maps to a value in a {@link RegistryEntryAttributeHolder}.
 *
 * @param <R> type of the entries in the registry
 * @param <V> attached value type
 */
@SuppressWarnings("ClassCanBeRecord")
public final class RegistryEntryAttribute<R, V> {
	/**
	 * Creates a new attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param codec value codec
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	public static <R, V> RegistryEntryAttribute<R, V> createWithDefault(Registry<R> registry, Identifier id, Codec<V> codec,
																		@Nullable V defaultValue) {
		return new RegistryEntryAttribute<>(registry, id, codec, defaultValue);
	}

	/**
	 * Creates a new attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param codec value codec
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	public static <R, V> RegistryEntryAttribute<R, V> create(Registry<R> registry, Identifier id, Codec<V> codec) {
		return createWithDefault(registry, id, codec, null);
	}

	/**
	 * Creates a new polymorphic attribute via dispatched codecs with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param codecGetter getter for a certain value type's codec
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	public static <R, V extends DispatchedType> RegistryEntryAttribute<R, V> createDispatchedWithDefault(Registry<R> registry, Identifier id,
																										 Function<Identifier, Codec<? extends V>> codecGetter,
																										 @Nullable V defaultValue) {
		return createWithDefault(registry, id, Identifier.CODEC.dispatch(V::getType, codecGetter), defaultValue);
	}

	/**
	 * Creates a new polymorphic attribute via dispatched codecs with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param codecGetter getter for a certain value type's codec
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	public static <R, V extends DispatchedType> RegistryEntryAttribute<R, V> createDispatched(Registry<R> registry, Identifier id,
																							  Function<Identifier, Codec<? extends V>> codecGetter) {
		return createDispatchedWithDefault(registry, id, codecGetter, null);
	}

	/**
	 * Creates a new {@code boolean} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Boolean> createBoolWithDefault(Registry<R> registry, Identifier id, boolean defaultValue) {
		return createWithDefault(registry, id, Codec.BOOL, defaultValue);
	}

	/**
	 * Creates a new {@code int} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Integer> createIntWithDefault(Registry<R> registry, Identifier id, int defaultValue) {
		return createWithDefault(registry, id, Codec.INT, defaultValue);
	}

	/**
	 * Creates a new {@code long} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Long> createLongWithDefault(Registry<R> registry, Identifier id, long defaultValue) {
		return createWithDefault(registry, id, Codec.LONG, defaultValue);
	}

	/**
	 * Creates a new {@code float} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Float> createFloatWithDefault(Registry<R> registry, Identifier id, float defaultValue) {
		return createWithDefault(registry, id, Codec.FLOAT, defaultValue);
	}

	/**
	 * Creates a new {@code double} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Double> createDoubleWithDefault(Registry<R> registry, Identifier id, double defaultValue) {
		return createWithDefault(registry, id, Codec.DOUBLE, defaultValue);
	}

	/**
	 * Creates a new {@code String} attribute with a default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, String> createStringWithDefault(Registry<R> registry, Identifier id, String defaultValue) {
		return createWithDefault(registry, id, Codec.STRING, defaultValue);
	}

	/**
	 * Creates a new {@code boolean} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Boolean> createBool(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.BOOL);
	}

	/**
	 * Creates a new {@code int} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Integer> createInt(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.INT);
	}


	/**
	 * Creates a new {@code long} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Long> createLong(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.LONG);
	}

	/**
	 * Creates a new {@code float} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Float> createFloat(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.FLOAT);
	}

	/**
	 * Creates a new {@code double} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, Double> createDouble(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.DOUBLE);
	}

	/**
	 * Creates a new {@code String} attribute with no default value.
	 *
	 * @param registry registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	public static <R> RegistryEntryAttribute<R, String> createString(Registry<R> registry, Identifier id) {
		return create(registry, id, Codec.STRING);
	}

	private final Registry<R> registry;
	private final Identifier id;
	private final Codec<V> codec;
	private final V defaultValue;

	private RegistryEntryAttribute(Registry<R> registry, Identifier id, Codec<V> codec, @Nullable V defaultValue) {
		this.registry = registry;
		this.id = id;
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
	public V getDefaultValue() {
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
		return RegistryEntryAttributeHolder.get(getRegistry()).getValue(entry, this);
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
