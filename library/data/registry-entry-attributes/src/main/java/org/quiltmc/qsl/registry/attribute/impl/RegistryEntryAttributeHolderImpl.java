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

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import java.util.Optional;

public class RegistryEntryAttributeHolderImpl<R> implements RegistryEntryAttributeHolder<R> {
	protected final Table<RegistryEntryAttribute<R, ?>, R, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	protected RegistryEntryAttributeHolderImpl() {
		valueTable = Tables.newCustomTable(new Object2ObjectOpenHashMap<>(), Reference2ObjectOpenHashMap::new);
	}

	@Override
	public <V> Optional<V> getValue(R entry, RegistryEntryAttribute<R, V> attribute) {
		var value = getValueNoDefault(entry, attribute);
		if (value == null) {
			return Optional.ofNullable(attribute.getDefaultValue());
		} else {
			return Optional.of(value);
		}
	}

	@SuppressWarnings("unchecked")
	public <V> V getValueNoDefault(R entry, RegistryEntryAttribute<R, V> attribute) {
		return (V) valueTable.get(attribute, entry);
	}

	public <T> void putValue(R entry, RegistryEntryAttribute<R, T> attribute, T value) {
		valueTable.put(attribute, entry, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
