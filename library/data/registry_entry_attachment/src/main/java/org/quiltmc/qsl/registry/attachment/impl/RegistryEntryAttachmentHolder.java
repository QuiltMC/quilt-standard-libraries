/*
 * Copyright 2021 The Quilt Project
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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public abstract class RegistryEntryAttachmentHolder<R> {
	public static <R> QuiltRegistryInternals<R> getInternals(Registry<R> registry) {
		return QuiltRegistryInternals.of(registry);
	}

	public static <R, T> void registerAttachment(Registry<R> registry, RegistryEntryAttachment<R, T> attachment) {
		getInternals(registry).quilt$registerAttachment(attachment);
	}

	public static <R> @Nullable RegistryEntryAttachment<R, ?> getAttachment(Registry<R> registry, Identifier id) {
		return getInternals(registry).quilt$getAttachment(id);
	}

	public static <R> Set<Map.Entry<Identifier, RegistryEntryAttachment<R, ?>>> getAttachmentEntries(Registry<R> registry) {
		return getInternals(registry).quilt$getAttachmentEntries();
	}

	/// impl for RegistryAttachment.get
	@SuppressWarnings("unchecked")
	public static <R, V> @Nullable RegistryEntryAttachment<R, V> getAttachment(Registry<R> registry, Identifier id, Class<V> valueClass) {
		var attachment = getAttachment(registry, id);
		if (attachment == null) {
			return null;
		}

		if (attachment.valueClass() != valueClass) {
			throw new IllegalArgumentException(("Found attachment with ID \"%s\" for registry \"%s\", "
					+ "but it has wrong value class (expected %s, got %s)")
					.formatted(id, registry.getKey().getValue(), valueClass, attachment.valueClass()));
		}

		return (RegistryEntryAttachment<R, V>) attachment;
	}

	public static <R> BuiltinRegistryEntryAttachmentHolder<R> getBuiltin(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getBuiltinAttachmentHolder();
		if (holder == null) {
			internals.quilt$setBuiltinAttachmentHolder(holder = new BuiltinRegistryEntryAttachmentHolder<>());
		}

		return holder;
	}

	public static <R> DataRegistryEntryAttachmentHolder<R> getData(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getDataAttachmentHolder();
		if (holder == null) {
			internals.quilt$setDataAttachmentHolder(holder = new DataRegistryEntryAttachmentHolder<>());
		}

		return holder;
	}

	public final Table<RegistryEntryAttachment<R, ?>, R, Object> valueTable;
	public final Table<RegistryEntryAttachment<R, ?>, TagKey<R>, Object> valueTagTable;

	@SuppressWarnings("UnstableApiUsage")
	protected RegistryEntryAttachmentHolder() {
		this.valueTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
		this.valueTagTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(RegistryEntryAttachment<R, V> attachment, R entry) {
		V value = (V) this.valueTable.get(attachment, entry); // Check for a direct value in valueTable
		if (value == null) { // If there is no value, check the valueTagTable
			Map<TagKey<R>, Object> row = this.valueTagTable.row(attachment);
			for (Map.Entry<TagKey<R>, Object> tagValue : row.entrySet()) { // Loop over the tags
				for (Holder<R> holder : attachment.registry().getTagOrEmpty(tagValue.getKey())) { // Loop over the holders in the tag
					if (holder.value().equals(entry)) { // The holder matches the entry
						if (value != null) { // Warn if two values pointing to the same entry are found.
							Initializer.LOGGER.warn("Entry {} for registry {} already has attachment {} defined. Overriding with value from tag {}.",
									attachment.registry().getId(entry),
									attachment.registry().getKey().getValue(),
									attachment.id(),
									tagValue.getKey().id());
						}

						value = (V) tagValue.getValue();
					}
				}
			}
		}

		return value;
	}

	public <T> void putValue(RegistryEntryAttachment<R, T> attachment, R entry, T value) {
		this.valueTable.put(attachment, entry, value);
	}

	public <T> void putValue(RegistryEntryAttachment<R, T> attachment, TagKey<R> tag, T value) {
		this.valueTagTable.put(attachment, tag, value);
	}

	public boolean removeValue(RegistryEntryAttachment<R, ?> attachment, R entry) {
		return this.valueTable.remove(attachment, entry) != null;
	}

	public boolean removeValue(RegistryEntryAttachment<R, ?> attachment, TagKey<R> tag) {
		return this.valueTagTable.remove(attachment, tag) != null;
	}
}
