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

package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolderImpl;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Optional;

/**
 * Maps {@link RegistryEntryAttribute}s to their values.
 *
 * @param <R> registry entry type
 */
public interface RegistryEntryAttributeHolder<R> {
	/**
	 * Gets the {@code RegistryEntryAttributeHolder} instance tied to a specific registry.
	 *
	 * @param registry registry
	 * @param <R> type of the entries in the registry
	 * @return the attribute holder tied to that registry
	 */
	static <R> RegistryEntryAttributeHolder<R> get(Registry<R> registry) {
		return RegistryEntryAttributeHolderImpl.getCombined(registry);
	}

	/**
	 * Gets the value associated with the specified attribute for the specified entry.<p>
	 *
	 * If the item has no value for this attribute, the attribute's
	 * {@linkplain RegistryEntryAttribute#getDefaultValue() default value} will be returned instead, unless it is {@code null},
	 * in which case an empty optional will be returned.
	 *
	 * @param entry registry entry
	 * @param attribute attribute
	 * @param <V> attribute's attached value type
	 * @return attribute value, or empty if no value is assigned
	 */
	<V> Optional<V> getValue(R entry, RegistryEntryAttribute<R, V> attribute);
}
