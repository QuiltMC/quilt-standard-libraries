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

package org.quiltmc.qsl.registry.attachment.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.attachment.impl.ComputedDefaultRegistryEntryAttachmentImpl;
import org.quiltmc.qsl.registry.attachment.impl.ConstantDefaultRegistryEntryAttachmentImpl;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;

/**
 * Represents an arbitrary value attached to a registry entry.
 *
 * @param <R> type of the entries in the registry
 * @param <V> attached value type
 */
@ApiStatus.NonExtendable
public interface RegistryEntryAttachment<R, V> extends Iterable<RegistryEntryAttachment.Entry<R, V>> {
	/**
	 * Retrieves an already-registered attachment.
	 *
	 * @param registry   registry attachment is attached to
	 * @param id         attachment identifier
	 * @param valueClass attached value class
	 * @param <R>        type of the entries in the registry
	 * @param <V>        attached value type
	 * @return the attachment, or empty if the attachment was not found
	 */
	static <R, V> Optional<RegistryEntryAttachment<R, V>> get(Registry<R> registry, Identifier id, Class<V> valueClass) {
		return Optional.ofNullable(RegistryEntryAttachmentHolder.getAttachment(registry, id, valueClass));
	}

	/**
	 * Creates a builder for an attachment.
	 *
	 * @param <R>        type of the entries in the registry
	 * @param <V>        attached value type
	 * @param registry   registry to attach to
	 * @param id         attachment identifier
	 * @param valueClass attached value class
	 * @param codec      attached value codec
	 * @return a builder
	 */
	static <R, V> Builder<R, V> builder(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec) {
		return new Builder<>(registry, id, valueClass, codec);
	}

	/**
	 * Creates a builder for an attachment using {@linkplain Codec#dispatch(Function, Function) dispatched codecs}
	 * for polymorphic types.
	 *
	 * @param registry   registry to attach to
	 * @param id         attachment identifier
	 * @param valueClass attached value class
	 * @param codec      type to codec mapper
	 * @param <R>        type of the entries in the registry
	 * @param <V>        attached value type
	 * @return a builder
	 */
	static <R, V extends DispatchedType> Builder<R, V> dispatchedBuilder(Registry<R> registry, Identifier id,
			Class<V> valueClass, Function<Identifier, Codec<? extends V>> codec) {
		return builder(registry, id, valueClass, Identifier.CODEC.dispatch(V::getType, codec));
	}

	/**
	 * Creates a builder for a boolean attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Boolean> boolBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Boolean.class, Codec.BOOL);
	}

	/**
	 * Creates a builder for an integer attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Integer> intBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Integer.class, Codec.INT);
	}

	/**
	 * Creates a builder for a long attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Long> longBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Long.class, Codec.LONG);
	}

	/**
	 * Creates a builder for a float attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Float> floatBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Float.class, Codec.FLOAT);
	}

	/**
	 * Creates a builder for a double attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, Double> doubleBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, Double.class, Codec.DOUBLE);
	}

	/**
	 * Creates a builder for a string attachment.
	 *
	 * @param registry registry to attach to
	 * @param id       attachment identifier
	 * @param <R>      type of the entries in the registry
	 * @return a builder
	 */
	static <R> Builder<R, String> stringBuilder(Registry<R> registry, Identifier id) {
		return builder(registry, id, String.class, Codec.STRING);
	}

	/**
	 * Gets the registry this attachment is attached to.
	 *
	 * @return attached registry
	 */
	Registry<R> registry();

	/**
	 * Gets the identifier of this attachment.
	 *
	 * @return the attachment identifier
	 */
	Identifier id();

	/**
	 * Gets the base class of this attachment's values.
	 *
	 * @return value class
	 */
	Class<V> valueClass();

	/**
	 * Gets the {@code Codec} used to (de)serialize this attachment's values.
	 *
	 * @return value codec
	 */
	Codec<V> codec();

	/**
	 * Gets the side this attachment should exist on.
	 *
	 * @return attachment side
	 */
	Side side();

	/**
	 * Gets the value associated with this attachment for the specified entry.
	 *
	 * @param entry registry entry
	 * @return attachment value, or {@code null} if no value is assigned
	 */
	@Nullable V getNullable(R entry);

	/**
	 * Gets the value associated with this attachment for the specified entry.
	 *
	 * @param entry registry entry
	 * @return attachment value, or empty if no value is assigned
	 */
	default Optional<V> get(R entry) {
		return Optional.ofNullable(this.getNullable(entry));
	}

	/**
	 * {@return a set of all registry entries with an associated value}
	 */
	Set<R> keySet();

	/**
	 * {@return a set of all tags with an associated value}
	 */
	Set<TagKey<R>> tagKeySet();

	/**
	 * {@return an iterator over all the values with a direct entry}
	 */
	Iterator<Entry<R, V>> entryIterator();

	/**
	 * {@return an iterator over all the tag values with a direct entry}
	 */
	Iterator<TagEntry<R, V>> tagEntryIterator();

	/**
	 * Associates a value with an entry.
	 * <p>
	 * <strong>NOTE:</strong> You should only call this method <em>before</em> registries are frozen!
	 * <br>
	 * Mods are allowed to ignore value associations that happen after registry freezing.
	 *
	 * @param entry registry entry
	 * @param value value
	 */
	void put(R entry, V value);

	/**
	 * Associates a value with a tag.
	 * <p>
	 * <strong>NOTE:</strong> You should only call this method <em>before</em> registries are frozen!
	 * <br>
	 * Mods are allowed to ignore value associations that happen after registry freezing.
	 *
	 * @param tag   tag
	 * @param value value
	 */
	void put(TagKey<R> tag, V value);

	/**
	 * Removes any value associated with an entry.
	 * <p>
	 * <strong>NOTE:</strong> You should only call this method <em>before</em> registries are frozen!
	 * <br>
	 * Mods are allowed to ignore value removals that happen after registry freezing.
	 *
	 * @param entry registry entry
	 * @return {@code true} if an associated value existed, or {@code false} otherwise
	 */
	boolean remove(R entry);

	/**
	 * Removes any value associated with a tag.
	 * <p>
	 * <strong>NOTE:</strong> You should only call this method <em>before</em> registries are frozen!
	 * <br>
	 * Mods are allowed to ignore value removals that happen after registry freezing.
	 *
	 * @param tag tag
	 * @return {@code true} if an associated value existed, or {@code false} otherwise
	 */
	boolean remove(TagKey<R> tag);

	/**
	 * {@return this attachment's "value associated with entry" event}
	 */
	Event<ValueAdded<R, V>> valueAddedEvent();

	/**
	 * {@return this attachment's "value associated with tag" event}
	 */
	Event<TagValueAdded<R, V>> tagValueAddedEvent();

	/**
	 * {@return this attachment's "entry's associated value removed" event}
	 */
	Event<ValueRemoved<R>> valueRemovedEvent();

	/**
	 * {@return this attachment's "tag's associated value removed" event}
	 */
	Event<TagValueRemoved<R>> tagValueRemovedEvent();

	/**
	 * Specifies on what side this attachment should exist.
	 */
	enum Side {
		/**
		 * This attachment is client-side only.
		 */
		CLIENT(ResourceType.CLIENT_RESOURCES),
		/**
		 * This attachment is server-side only.
		 */
		SERVER(ResourceType.SERVER_DATA),
		/**
		 * This attachment exists on both sides. Its values will be synchronized from server to client.
		 */
		BOTH(ResourceType.SERVER_DATA);

		private final ResourceType source;

		Side(ResourceType source) {
			this.source = source;
		}

		/**
		 * Gets the source attachments of this side should be loaded from.
		 *
		 * @return source to use for attachments of this side
		 */
		public ResourceType getSource() {
			return this.source;
		}

		/**
		 * Checks if attachments of this side should load from this source.
		 *
		 * @param source the source to check
		 * @return if the attachment value should be loaded or not
		 */
		public boolean shouldLoad(ResourceType source) {
			return this.source == source;
		}
	}

	/**
	 * Specifies a value association entry.
	 *
	 * @param entry the registry entry
	 * @param value the associated value
	 * @param <R>   type of registry entry
	 * @param <V>   type of value
	 */
	record Entry<R, V>(R entry, V value) {
		/**
		 * Creates a new entry.
		 *
		 * @param entry the registry entry
		 * @param value the associated value
		 */
		public Entry {}
	}

	/**
	 * Specifies a tag value association entry.
	 *
	 * @param tag   the tag
	 * @param value the associated value
	 * @param <R>   type of registry entry
	 * @param <V>   type of value
	 */
	record TagEntry<R, V>(TagKey<R> tag, V value) {
		/**
		 * Creates a new entry.
		 *
		 * @param tag   the tag
		 * @param value the associated value
		 */
		public TagEntry {}
	}

	/**
	 * Event that is fired on a value being associated with an entry.
	 *
	 * @param <R> type of the entries in the registry
	 * @param <V> attached value type
	 */
	@FunctionalInterface
	interface ValueAdded<R, V> {
		void onValueAdded(R entry, V value);
	}

	/**
	 * Event that is fired on a value being associated with a tag.
	 *
	 * @param <R> type of the entries in the registry
	 * @param <V> attached value type
	 */
	@FunctionalInterface
	interface TagValueAdded<R, V> {
		void onTagValueAdded(TagKey<R> tag, V value);
	}

	/**
	 * Event that is fired on an entry's associated value being removed.
	 *
	 * @param <R> attached value type
	 */
	@FunctionalInterface
	interface ValueRemoved<R> {
		void onValueRemoved(R entry);
	}

	/**
	 * Event that is fired on a tag's associated value being removed.
	 *
	 * @param <R> attached value type
	 */
	@FunctionalInterface
	interface TagValueRemoved<R> {
		void onTagValueRemoved(TagKey<R> tag);
	}

	/**
	 * Builder for attachments.
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
			this.side = Side.BOTH;

			if (RegistryEntryAttachmentHolder.getAttachment(registry, id) != null) {
				throw new IllegalStateException("Attachment with ID '%s' is already registered for registry %s!"
						.formatted(id, registry.getKey().getValue()));
			}
		}

		/**
		 * Sets what side this attachment should exist on.
		 *
		 * @param side attachment side
		 * @return this builder
		 */
		public Builder<R, V> side(Side side) {
			this.side = side;
			return this;
		}

		/**
		 * Sets the default value of this attachment.
		 * <p>
		 * Setting this will <b>remove</b> the currently set
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
		 * Sets the <em>default value provider</em> of this attachment, which will be used to compute a value for a
		 * specific entry, should it be missing.
		 * <p>
		 * Note that this will be computed on both sides and the computation result will <em>not</em> be synchronized.
		 * <p>
		 * Setting this will <b>remove</b> the currently set
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
		 * Builds a new attachment.
		 *
		 * @return new attachment
		 */
		public RegistryEntryAttachment<R, V> build() {
			RegistryEntryAttachment<R, V> attachment;
			if (this.defaultValueProvider == null) {
				attachment = new ConstantDefaultRegistryEntryAttachmentImpl<>(this.registry, this.id, this.valueClass,
						this.codec, this.side, this.defaultValue);
			} else {
				attachment = new ComputedDefaultRegistryEntryAttachmentImpl<>(this.registry, this.id, this.valueClass,
						this.codec, this.side, this.defaultValueProvider);
			}
			RegistryEntryAttachmentHolder.registerAttachment(this.registry, attachment);
			return attachment;
		}
	}
}
