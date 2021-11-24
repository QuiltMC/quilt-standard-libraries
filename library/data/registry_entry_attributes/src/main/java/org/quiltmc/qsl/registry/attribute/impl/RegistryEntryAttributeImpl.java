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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@ApiStatus.Internal
public record RegistryEntryAttributeImpl<R, V>(Registry<R> registry,
											   Identifier id,
											   Side side,
											   Codec<V> codec,
											   @Nullable V defaultValue,
											   @Nullable Function<R, V> missingValueFunction)
		implements RegistryEntryAttribute<R, V> {

	@Override
	public Optional<V> getValue(R entry) {
		V value;
		if (side == Side.CLIENT) {
			AssetsHolderGuard.assertAccessAllowed();
			value = RegistryEntryAttributeHolder.getAssets(registry).getValue(this, entry);
			if (value != null) {
				return Optional.of(value);
			}
		}
		value = RegistryEntryAttributeHolder.getData(registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}
		value = RegistryEntryAttributeHolder.getBuiltin(registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}
		if (missingValueFunction != null) {
			value = missingValueFunction.apply(entry);
			if (value == null) {
				return Optional.empty();
			} else {
				RegistryEntryAttributeHolder.getBuiltin(registry).putValue(this, entry, value);
				return Optional.of(value);
			}
		}
		return Optional.ofNullable(defaultValue);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttributeImpl<?, ?> that)) return false;
		return Objects.equals(registry.getKey(), that.registry.getKey()) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registry.getKey(), id);
	}

	@Override
	public String toString() {
		return "RegistryEntryAttributeImpl{" +
				"registry=" + registry +
				", id=" + id +
				'}';
	}
}
