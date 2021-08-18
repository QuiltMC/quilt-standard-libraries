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
import java.util.Map;

public interface RegistryEntryAttribute<R, T> {
	static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec,
													  @Nullable T defaultValue) {
		return RegistryEntryAttributeImpl.create(registryKey, id, codec, defaultValue);
	}

	static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec) {
		return create(registryKey, id, codec, null);
	}

	static <R, T extends DispatchedType> RegistryEntryAttribute<R, T> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Map<String, Codec<? extends T>> codecs, @Nullable T defaultValue) {
		return create(registryKey, id, Codec.STRING.dispatch(T::getType, codecs::get), defaultValue);
	}

	static <R, T extends DispatchedType> RegistryEntryAttribute<R, T> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Map<String, Codec<? extends T>> codecs) {
		return createDispatched(registryKey, id, codecs, null);
	}

	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id, boolean defaultValue) {
		return create(registryKey, id, Codec.BOOL, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id, int defaultValue) {
		return create(registryKey, id, Codec.INT, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id, long defaultValue) {
		return create(registryKey, id, Codec.LONG, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id, float defaultValue) {
		return create(registryKey, id, Codec.FLOAT, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id, double defaultValue) {
		return create(registryKey, id, Codec.DOUBLE, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.BOOL, null);
	}

	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.INT, null);
	}

	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.LONG, null);
	}

	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.FLOAT, null);
	}

	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.DOUBLE, null);
	}

	RegistryKey<Registry<R>> getRegistryKey();
	Identifier getId();
	Codec<T> getCodec();
	@Nullable T getDefaultValue();
}
