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

package org.quiltmc.qsl.registry.attribute.impl;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class RegistryEntryAttributeImpl<R, V> implements RegistryEntryAttribute<R, V> {
	private final RegistryKey<Registry<R>> registryKey;
	private final Identifier id;
	private final Codec<V> codec;
	private final @Nullable V defaultValue;

	private RegistryEntryAttributeImpl(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<V> codec, @Nullable V defaultValue) {
		this.registryKey = registryKey;
		this.id = id;
		this.codec = codec;
		this.defaultValue = defaultValue;
	}

	public static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec,
															 @Nullable T defaultValue) {
		var attrib = new RegistryEntryAttributeImpl<>(registryKey, id, codec, defaultValue);
		@SuppressWarnings("unchecked") var registry = (Registry<R>) Registry.REGISTRIES.get(registryKey.getValue());
		RegistryEntryAttributeHolderImpl.registerAttribute(registry, attrib);
		return attrib;
	}

	@Override
	public RegistryKey<Registry<R>> getRegistryKey() {
		return registryKey;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public Codec<V> getCodec() {
		return codec;
	}

	@Override
	public @Nullable V getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttributeImpl<?, ?> that)) return false;
		return Objects.equals(registryKey, that.registryKey) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registryKey, id);
	}
}
