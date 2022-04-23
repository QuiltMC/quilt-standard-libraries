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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public final class RegistryEntryAttachmentHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> QuiltRegistryInternals<R> getInternals(Registry<R> registry) {
		return (QuiltRegistryInternals<R>) registry;
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
			throw new IllegalArgumentException(("Found attachment with ID \"%s\" for registry \"%s\", " +
					"but it has wrong value class (expected %s, got %s)")
					.formatted(id, registry.getKey().getValue(), valueClass, attachment.valueClass()));
		}
		return (RegistryEntryAttachment<R, V>) attachment;
	}

	public static <R> RegistryEntryAttachmentHolder<R> getBuiltin(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getBuiltinAttachmentHolder();
		if (holder == null) {
			internals.quilt$setBuiltinAttachmentHolder(holder = new RegistryEntryAttachmentHolder<>());
		}
		return holder;
	}

	public static <R> RegistryEntryAttachmentHolder<R> getData(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getDataAttachmentHolder();
		if (holder == null) {
			internals.quilt$setDataAttachmentHolder(holder = new RegistryEntryAttachmentHolder<>());
		}
		return holder;
	}

	public static <R> RegistryEntryAttachmentHolder<R> getAssets(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getAssetsAttachmentHolder();
		if (holder == null) {
			internals.quilt$setAssetsAttachmentHolder(holder = new RegistryEntryAttachmentHolder<>());
		}
		return holder;
	}

	public final Table<RegistryEntryAttachment<R, ?>, R, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	private RegistryEntryAttachmentHolder() {
		this.valueTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(RegistryEntryAttachment<R, V> attachment, R entry) {
		return (V) this.valueTable.get(attachment, entry);
	}

	public <T> void putValue(RegistryEntryAttachment<R, T> attachment, R entry, T value) {
		this.valueTable.put(attachment, entry, value);
	}

	public void clear() {
		this.valueTable.clear();
	}
}
