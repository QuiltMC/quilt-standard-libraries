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

import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.Optional;

public interface RegistryEntryAttributeHolder<R> {
	@SuppressWarnings("unchecked")
	static <R> QuiltRegistryInternals<R> getInternals(Registry<R> registry) {
		return (QuiltRegistryInternals<R>) registry;
	}

	static <R, T> void registerAttribute(Registry<R> registry, RegistryEntryAttribute<R, T> attribute) {
		getInternals(registry).qsl$registerAttribute(attribute);
	}

	static <R> @Nullable RegistryEntryAttribute<R, ?> getAttribute(Registry<R> registry, Identifier id) {
		return getInternals(registry).qsl$getAttribute(id);
	}

	static <R> RegistryEntryAttributeHolder<R> getCombined(Registry<R> registry) {
		return getInternals(registry).qsl$getCombinedAttributeHolder();
	}

	static <R> RegistryEntryAttributeHolderImpl<R> getBuiltin(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.qsl$getBuiltinAttributeHolder();
		if (holder == null) {
			internals.qsl$setBuiltinAttributeHolder(holder = new RegistryEntryAttributeHolderImpl<>());
		}
		return holder;
	}

	static <R> RegistryEntryAttributeHolderImpl<R> getData(Registry<R> registry) {
		var internals = getInternals(registry);
		var holder = internals.qsl$getDataAttributeHolder();
		if (holder == null) {
			internals.qsl$setDataAttributeHolder(holder = new RegistryEntryAttributeHolderImpl<>());
		}
		return holder;
	}

	<V> Optional<V> getValue(R entry, RegistryEntryAttribute<R, V> attribute);
}
