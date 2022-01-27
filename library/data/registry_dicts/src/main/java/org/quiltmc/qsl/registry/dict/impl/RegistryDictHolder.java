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

package org.quiltmc.qsl.registry.dict.impl;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;
import org.quiltmc.qsl.registry.dict.api.WrongValueClassException;

@ApiStatus.Internal
public final class RegistryDictHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> QuiltRegistryInternals<R> getInternals(Registry<R> registry) {
		return (QuiltRegistryInternals<R>) registry;
	}

	public static <R, T> void registerDict(Registry<R> registry, RegistryDict<R, T> dictionary) {
		getInternals(registry).quilt$registerDict(dictionary);
	}

	public static <R> @Nullable RegistryDict<R, ?> getDict(Registry<R> registry, Identifier id) {
		return getInternals(registry).quilt$getDict(id);
	}

	public static <R> Set<Map.Entry<Identifier, RegistryDict<R, ?>>> getDictEntries(Registry<R> registry) {
		return getInternals(registry).quilt$getDictEntries();
	}

	/// impl for RegistryDict.get
	@SuppressWarnings("unchecked")
	public static <R, V> @Nullable RegistryDict<R, V> getDict(Registry<R> registry, Identifier id, Class<V> valueClass)
			throws WrongValueClassException {
		var dict = getDict(registry, id);
		if (dict == null) {
			return null;
		}
		if (dict.valueClass() != valueClass) {
			throw new WrongValueClassException(registry, id, valueClass, dict.valueClass());
		}
		return (RegistryDict<R, V>) dict;
	}

	public static <R> RegistryDictHolder<R> getBuiltin(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getBuiltinDictHolder();
		if (holder == null) {
			internals.quilt$setBuiltinDictHolder(holder = new RegistryDictHolder<>());
		}
		return holder;
	}

	public static <R> RegistryDictHolder<R> getData(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getDataDictHolder();
		if (holder == null) {
			internals.quilt$setDataDictHolder(holder = new RegistryDictHolder<>());
		}
		return holder;
	}

	public static <R> RegistryDictHolder<R> getAssets(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.quilt$getAssetsDictHolder();
		if (holder == null) {
			internals.quilt$setAssetsDictHolder(holder = new RegistryDictHolder<>());
		}
		return holder;
	}

	public final Table<RegistryDict<R, ?>, R, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	private RegistryDictHolder() {
		valueTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(RegistryDict<R, V> dictionary, R entry) {
		return (V) valueTable.get(dictionary, entry);
	}

	public <T> void putValue(RegistryDict<R, T> dictionary, R entry, T value) {
		valueTable.put(dictionary, entry, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
