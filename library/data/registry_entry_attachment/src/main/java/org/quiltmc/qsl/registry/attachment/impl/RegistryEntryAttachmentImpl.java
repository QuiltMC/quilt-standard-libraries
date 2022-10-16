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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashBigSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	protected final Event<ValueRemoved<R>> valueRemovedEvent;
	protected final Event<TagValueRemoved<R>> tagValueRemovedEvent;

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
		this.tagValueAddedEvent = Event.create(TagValueAdded.class, listeners -> (tag, value) -> {
			for (var listener : listeners) {
				listener.onTagValueAdded(tag, value);
			}
		});
		this.valueRemovedEvent = Event.create(ValueRemoved.class, listeners -> entry -> {
			for (var listener : listeners) {
				listener.onValueRemoved(entry);
			}
		});
		this.tagValueRemovedEvent = Event.create(TagValueRemoved.class, listeners -> tag -> {
			for (var listener : listeners) {
				listener.onTagValueRemoved(tag);
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

	protected abstract @Nullable V getDefaultValue(R entry);

	@Override
	public @Nullable V getNullable(R entry) {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		V value = RegistryEntryAttachmentHolder.getData(this.registry).getValue(this, entry);
		if (value != null) {
			return value;
		}

		value = RegistryEntryAttachmentHolder.getBuiltin(this.registry).getValue(this, entry);
		if (value != null) {
			return value;
		}

		return this.getDefaultValue(entry);
	}

	@Override
	public Set<R> keySet() {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		Set<R> set = new ReferenceOpenHashBigSet<>();
		set.addAll(RegistryEntryAttachmentHolder.getData(this.registry).valueTable.row(this).keySet());
		set.addAll(RegistryEntryAttachmentHolder.getBuiltin(this.registry).valueTable.row(this).keySet());
		return set;
	}

	@Override
	public Set<TagKey<R>> tagKeySet() {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		Set<TagKey<R>> set = new ReferenceOpenHashBigSet<>();
		set.addAll(RegistryEntryAttachmentHolder.getData(this.registry).valueTagTable.row(this).keySet());
		set.addAll(RegistryEntryAttachmentHolder.getBuiltin(this.registry).valueTagTable.row(this).keySet());
		return set;
	}

	@Override
	public @NotNull Iterator<Entry<R, V>> iterator() {
		return RegistryEntryAttachmentImpl.this.registry.stream()
				.map(r -> {
					V value = RegistryEntryAttachmentImpl.this.getNullable(r);
					return value == null ? null : new Entry<>(r, value);
				})
				.filter(Objects::nonNull)
				.iterator();
	}

	@Override
	public Iterator<Entry<R, V>> entryIterator() {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		return new EntryIterator();
	}

	@Override
	public Iterator<TagEntry<R, V>> tagEntryIterator() {
		if (this.side == Side.CLIENT) {
			ClientSideGuard.assertAccessAllowed();
		}

		return new TagEntryIterator();
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
	public boolean remove(R entry) {
		if (RegistryEntryAttachmentHolder.getBuiltin(this.registry).removeValue(this, entry)) {
			this.valueRemovedEvent.invoker().onValueRemoved(entry);
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(TagKey<R> tag) {
		if (RegistryEntryAttachmentHolder.getBuiltin(this.registry).removeValue(this, tag)) {
			this.tagValueRemovedEvent.invoker().onTagValueRemoved(tag);
			return true;
		}

		return false;
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
	public Event<ValueRemoved<R>> valueRemovedEvent() {
		return valueRemovedEvent;
	}

	@Override
	public Event<TagValueRemoved<R>> tagValueRemovedEvent() {
		return tagValueRemovedEvent;
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

	protected class EntryIterator implements Iterator<Entry<R, V>> {
		protected final RegistryEntryAttachmentHolder<R> dataHolder, builtinHolder;
		protected Iterator<R> keyIt;

		public EntryIterator() {
			this.dataHolder = RegistryEntryAttachmentHolder.getData(RegistryEntryAttachmentImpl.this.registry);
			this.builtinHolder = RegistryEntryAttachmentHolder.getBuiltin(RegistryEntryAttachmentImpl.this.registry);
			this.keyIt = RegistryEntryAttachmentImpl.this.keySet().iterator();
		}

		@Override
		public boolean hasNext() {
			return keyIt.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<R, V> next() {
			R key = keyIt.next();
			V value = (V) dataHolder.valueTable.get(RegistryEntryAttachmentImpl.this, key);
			if (value == null) {
				value = (V) builtinHolder.valueTable.get(RegistryEntryAttachmentImpl.this, key);
			}

			return new Entry<>(key, value);
		}
	}

	protected class TagEntryIterator implements Iterator<TagEntry<R, V>> {
		protected final RegistryEntryAttachmentHolder<R> dataHolder, builtinHolder;
		protected Iterator<TagKey<R>> keyIt;

		public TagEntryIterator() {
			this.dataHolder = RegistryEntryAttachmentHolder.getData(RegistryEntryAttachmentImpl.this.registry);
			this.builtinHolder = RegistryEntryAttachmentHolder.getBuiltin(RegistryEntryAttachmentImpl.this.registry);
			this.keyIt = RegistryEntryAttachmentImpl.this.tagKeySet().iterator();
		}

		@Override
		public boolean hasNext() {
			return keyIt.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public TagEntry<R, V> next() {
			TagKey<R> key = keyIt.next();
			V value = (V) dataHolder.valueTagTable.get(RegistryEntryAttachmentImpl.this, key);
			if (value == null) {
				value = (V) builtinHolder.valueTagTable.get(RegistryEntryAttachmentImpl.this, key);
			}

			return new TagEntry<>(key, value);
		}
	}
}
