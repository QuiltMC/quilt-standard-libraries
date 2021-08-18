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
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.Optional;

public class RegistryEntryAttributeHolderImpl<R> implements RegistryEntryAttributeHolder<R> {
	public static <R, T> void registerAttribute(Registry<R> registry, RegistryEntryAttribute<R, T> attribute) {
		((QuiltRegistryInternals) registry).qsl$registerAttribute(attribute);
	}

	@SuppressWarnings("unchecked")
	public static <R> @Nullable RegistryEntryAttribute<R, ?> getAttribute(Registry<R> registry, Identifier id) {
		return (RegistryEntryAttribute<R, ?>) ((QuiltRegistryInternals) registry).qsl$getAttribute(id);
	}

	@SuppressWarnings("unchecked")
	public static <R> RegistryEntryAttributeHolder<R> getCombined(Registry<R> registry) {
		return (RegistryEntryAttributeHolder<R>) ((QuiltRegistryInternals) registry).qsl$getCombinedAttributeHolder();
	}

	@SuppressWarnings("unchecked")
	public static <R> RegistryEntryAttributeHolderImpl<R> getBuiltin(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getBuiltinAttributeHolder();
		if (holder == null) {
			internals.qsl$setBuiltinAttributeHolder(holder = new RegistryEntryAttributeHolderImpl<>());
		}
		return (RegistryEntryAttributeHolderImpl<R>) holder;
	}

	@SuppressWarnings("unchecked")
	public static <R> RegistryEntryAttributeHolderImpl<R> getData(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getDataAttributeHolder();
		if (holder == null) {
			internals.qsl$setDataAttributeHolder(holder = new RegistryEntryAttributeHolderImpl<>());
		}
		return (RegistryEntryAttributeHolderImpl<R>) holder;
	}

	protected final Table<R, RegistryEntryAttribute<R, ?>, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	protected RegistryEntryAttributeHolderImpl() {
		valueTable = Tables.newCustomTable(new Reference2ObjectOpenHashMap<>(), Object2ObjectOpenHashMap::new);
	}

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		var value = getValueNoDefault(item, attribute);
		if (value == null) {
			return Optional.ofNullable(attribute.getDefaultValue());
		} else {
			return Optional.of(value);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getValueNoDefault(R item, RegistryEntryAttribute<R, T> attribute) {
		return (T) valueTable.get(item, attribute);
	}

	public <T> void putValue(R item, RegistryEntryAttribute<R, T> attribute, T value) {
		valueTable.put(item, attribute, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
