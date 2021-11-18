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

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;

@ApiStatus.Internal
public final class RegistryEntryAttributeHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> QuiltRegistryInternals<R> getInternals(Registry<R> registry) {
		return (QuiltRegistryInternals<R>) registry;
	}

	public static <R, T> void registerAttribute(Registry<R> registry, RegistryEntryAttribute<R, T> attribute) {
		getInternals(registry).quilt$registerAttribute(attribute);
	}

	public static <R> @Nullable RegistryEntryAttribute<R, ?> getAttribute(Registry<R> registry, Identifier id) {
		return getInternals(registry).quilt$getAttribute(id);
	}

	public static <R> RegistryEntryAttributeHolder<R> getBuiltin(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getBuiltinAttributeHolder();
		if (holder == null) {
			internals.quilt$setBuiltinAttributeHolder(holder = new RegistryEntryAttributeHolder<>());
		}
		return holder;
	}

	public static <R> RegistryEntryAttributeHolder<R> getData(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getDataAttributeHolder();
		if (holder == null) {
			internals.quilt$setDataAttributeHolder(holder = new RegistryEntryAttributeHolder<>());
		}
		return holder;
	}

	public static <R> RegistryEntryAttributeHolder<R> getAssets(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getAssetsAttributeHolder();
		if (holder == null) {
			internals.quilt$setAssetsAttributeHolder(holder = new RegistryEntryAttributeHolder<>());
		}
		return holder;
	}

	public final Table<RegistryEntryAttribute<R, ?>, R, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	private RegistryEntryAttributeHolder() {
		valueTable = Tables.newCustomTable(new Object2ObjectOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(RegistryEntryAttribute<R, V> attribute, R entry) {
		return (V) valueTable.get(attribute, entry);
	}

	public <T> void putValue(RegistryEntryAttribute<R, T> attribute, R entry, T value) {
		valueTable.put(attribute, entry, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
