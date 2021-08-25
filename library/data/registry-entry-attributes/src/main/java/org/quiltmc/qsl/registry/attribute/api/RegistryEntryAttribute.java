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

import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents an attribute that is attached to a registry entry. Maps to a value in a {@link RegistryEntryAttributeHolder}.
 *
 * @param <R> registry entry type
 * @param <V> value type
 */
public interface RegistryEntryAttribute<R, V> {
	/**
	 * Creates a new attribute.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param codec value codec
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	static <R, V> RegistryEntryAttribute<R, V> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<V> codec,
													  @Nullable V defaultValue) {
		return RegistryEntryAttributeImpl.create(registryKey, id, codec, defaultValue);
	}

	/**
	 * Creates a new attribute.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param codec value codec
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	static <R, V> RegistryEntryAttribute<R, V> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<V> codec) {
		return create(registryKey, id, codec, null);
	}

	/**
	 * Creates a new polymorphic attribute via dispatched codecs.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param codecGetter getter for a certain value type's codec
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	static <R, V extends DispatchedType> RegistryEntryAttribute<R, V> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Function<Identifier, Codec<? extends V>> codecGetter,
																					   @Nullable V defaultValue) {
		return create(registryKey, id, Identifier.CODEC.dispatch(V::getType, codecGetter), defaultValue);
	}

	/**
	 * Creates a new polymorphic attribute via dispatched codecs.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param codecGetter getter for a certain value type's codec
	 * @param <R> registry entry type
	 * @param <V> value type
	 * @return a new attribute
	 */
	static <R, V extends DispatchedType> RegistryEntryAttribute<R, V> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Function<Identifier, Codec<? extends V>> codecGetter) {
		return createDispatched(registryKey, id, codecGetter, null);
	}

	/**
	 * Creates a new {@code boolean} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id, boolean defaultValue) {
		return create(registryKey, id, Codec.BOOL, defaultValue);
	}

	/**
	 * Creates a new {@code int} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id, int defaultValue) {
		return create(registryKey, id, Codec.INT, defaultValue);
	}

	/**
	 * Creates a new {@code long} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id, long defaultValue) {
		return create(registryKey, id, Codec.LONG, defaultValue);
	}

	/**
	 * Creates a new {@code float} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id, float defaultValue) {
		return create(registryKey, id, Codec.FLOAT, defaultValue);
	}

	/**
	 * Creates a new {@code double} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id, double defaultValue) {
		return create(registryKey, id, Codec.DOUBLE, defaultValue);
	}

	/**
	 * Creates a new {@code String} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param defaultValue default value
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, String> createString(RegistryKey<Registry<R>> registryKey, Identifier id, String defaultValue) {
		return create(registryKey, id, Codec.STRING, defaultValue);
	}

	/**
	 * Creates a new {@code boolean} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.BOOL, null);
	}

	/**
	 * Creates a new {@code int} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.INT, null);
	}


	/**
	 * Creates a new {@code long} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.LONG, null);
	}

	/**
	 * Creates a new {@code float} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.FLOAT, null);
	}

	/**
	 * Creates a new {@code double} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.DOUBLE, null);
	}

	/**
	 * Creates a new {@code String} property.
	 *
	 * @param registryKey key of registry this attribute is tied to
	 * @param id identifier
	 * @param <R> registry entry type
	 * @return a new attribute
	 */
	static <R> RegistryEntryAttribute<R, String> createString(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.STRING, null);
	}

	/**
	 * Gets the key of the registry this attribute is tied to.
	 *
	 * @return tied registry key
	 */
	RegistryKey<Registry<R>> getRegistryKey();

	/**
	 * Gets the ID of this attribute.
	 *
	 * @return attribute ID
	 */
	Identifier getId();

	/**
	 * Gets the {@code Codec} used to (de)serialize this attribute's value.
	 *
	 * @return value codec
	 */
	Codec<V> getCodec();

	/**
	 * Gets the default value of this attribute. Can be {@code null}.
	 *
	 * @return default value
	 */
	@Nullable V getDefaultValue();

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
	default Optional<V> getValue(R entry) {
		return RegistryEntryAttributeHolder.get(getRegistryKey()).getValue(entry, this);
	}
}
