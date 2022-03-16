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

package org.quiltmc.qsl.registry.attachment.impl;

import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public abstract class RegistryEntryAttachmentImpl<R, V> implements RegistryEntryAttachment<R, V> {
	protected final Registry<R> registry;
	protected final Identifier id;
	protected final Class<V> valueClass;
	protected final Codec<V> codec;
	protected final Side side;

	public RegistryEntryAttachmentImpl(Registry<R> registry,
									   Identifier id,
									   Class<V> valueClass,
									   Codec<V> codec,
									   Side side) {
		this.registry = registry;
		this.id = id;
		this.valueClass = valueClass;
		this.codec = codec;
		this.side = side;
	}

	@Override
	public Registry<R> registry() {
		return registry;
	}

	@Override
	public Identifier id() {
		return id;
	}

	@Override
	public Class<V> valueClass() {
		return valueClass;
	}

	@Override
	public Codec<V> codec() {
		return codec;
	}

	@Override
	public Side side() {
		return side;
	}

	protected abstract Optional<V> getDefaultValue(R entry);

	@Override
	public Optional<V> getValue(R entry) {
		V value;
		if (side == Side.CLIENT) {
			AssetsHolderGuard.assertAccessAllowed();
			value = RegistryEntryAttachmentHolder.getAssets(registry).getValue(this, entry);
			if (value != null) {
				return Optional.of(value);
			}
		}
		value = RegistryEntryAttachmentHolder.getData(registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}
		value = RegistryEntryAttachmentHolder.getBuiltin(registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}
		return getDefaultValue(entry);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttachmentImpl<?, ?> that)) return false;
		return Objects.equals(registry.getKey(), that.registry.getKey()) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registry.getKey(), id);
	}

	@Override
	public String toString() {
		return "RegistryAttachment{" +
				"registry=" + registry +
				", id=" + id +
				", valueClass=" + valueClass +
				'}';
	}
}
