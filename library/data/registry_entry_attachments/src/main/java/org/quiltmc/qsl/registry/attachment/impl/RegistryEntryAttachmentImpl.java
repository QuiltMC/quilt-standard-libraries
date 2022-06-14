/*
 * Copyright 2021-2022 QuiltMC
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

import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public abstract class RegistryEntryAttachmentImpl<R, V> implements RegistryEntryAttachment<R, V> {
	protected final Registry<R> registry;
	protected final Identifier id;
	protected final Class<V> valueClass;
	protected final Codec<V> codec;
	protected final Side side;
	protected final Event<ValueAdded<R, V>> valueAddedEvent;
	protected final Event<TagValueAdded<R, V>> tagValueAddedEvent;

	public RegistryEntryAttachmentImpl(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec,
			Side side) {
		this.registry = registry;
		this.id = id;
		this.valueClass = valueClass;
		this.codec = codec;
		this.side = side;

		this.valueAddedEvent = Event.create(ValueAdded.class, listeners -> (entry, value) -> {
			for (var listener : listeners) {
				listener.onValueAdded(entry, value);
			}
		});
		this.tagValueAddedEvent = Event.create(TagValueAdded.class, listeners -> (entry, value) -> {
			for (var listener : listeners) {
				listener.onTagValueAdded(entry, value);
			}
		});
	}

	@Override
	public Registry<R> registry() {
		return this.registry;
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public Class<V> valueClass() {
		return this.valueClass;
	}

	@Override
	public Codec<V> codec() {
		return this.codec;
	}

	@Override
	public Side side() {
		return this.side;
	}

	protected abstract Optional<V> getDefaultValue(R entry);

	@Override
	public Optional<V> get(R entry) {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		V value = RegistryEntryAttachmentHolder.getData(this.registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}

		value = RegistryEntryAttachmentHolder.getBuiltin(this.registry).getValue(this, entry);
		if (value != null) {
			return Optional.of(value);
		}

		return this.getDefaultValue(entry);
	}

	@Override
	public void put(R entry, V value) {
		CodecUtils.assertValid(this.codec, value);
		RegistryEntryAttachmentHolder.getBuiltin(this.registry).putValue(this, entry, value,
				BuiltinRegistryEntryAttachmentHolder.FLAG_NONE);
		this.valueAddedEvent.invoker().onValueAdded(entry, value);
	}

	@Override
	public void put(TagKey<R> entry, V value) {
		CodecUtils.assertValid(this.codec, value);
		RegistryEntryAttachmentHolder.getBuiltin(this.registry).putValue(this, entry, value);
		this.tagValueAddedEvent.invoker().onTagValueAdded(entry, value);
	}

	@Override
	public Event<ValueAdded<R, V>> valueAddedEvent() {
		return this.valueAddedEvent;
	}

	@Override
	public Event<TagValueAdded<R, V>> tagValueAddedEvent() {
		return this.tagValueAddedEvent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttachmentImpl<?, ?> that)) return false;
		return Objects.equals(this.registry.getKey(), that.registry.getKey()) && Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.registry.getKey(), this.id);
	}

	@Override
	public String toString() {
		return "RegistryAttachment{" +
				"registry=" + this.registry +
				", id=" + this.id +
				", valueClass=" + this.valueClass +
				'}';
	}
}
