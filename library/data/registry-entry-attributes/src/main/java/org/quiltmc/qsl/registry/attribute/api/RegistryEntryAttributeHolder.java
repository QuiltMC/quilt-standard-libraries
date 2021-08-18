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

public interface RegistryEntryAttributeHolder<R> {
	static <R> RegistryEntryAttributeHolder<R> get(Registry<R> registry) {
		return RegistryEntryAttributeHolderImpl.getCombined(registry);
	}

	@SuppressWarnings("unchecked")
	static <R> RegistryEntryAttributeHolder<R> get(RegistryKey<Registry<R>> registryKey) {
		Registry<R> registry = (Registry<R>) Registry.REGISTRIES.get(registryKey.getValue());
		return get(registry);
	}

	<T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute);
}
