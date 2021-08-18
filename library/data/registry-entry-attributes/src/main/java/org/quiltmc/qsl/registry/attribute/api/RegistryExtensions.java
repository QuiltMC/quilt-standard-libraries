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

import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryEntryAttributeSetterImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.function.Consumer;

/**
 * Extensions for working with {@link Registry}s.
 */
public final class RegistryExtensions {
	/**
	 * Registers an entry into a registry, also setting built-in attribute values for it.
	 *
	 * @param registry target registry
	 * @param id entry identifier
	 * @param entry entry
	 * @param setterConsumer setter consumer
	 * @param <R> registry entry type
	 * @param <T> current entry type
	 * @return the newly registered entry
	 */
	public static <R, T extends R> T registerWithAttributes(Registry<R> registry, Identifier id, T entry,
												  Consumer<AttributeSetter<R>> setterConsumer) {
		Registry.register(registry, id, entry);
		setterConsumer.accept(new BuiltinRegistryEntryAttributeSetterImpl<>(registry, entry));
		return entry;
	}

	/**
	 * Used to set built-in attribute values in a builder-like fashion.
	 *
	 * @param <R> registry entry type
	 */
	@FunctionalInterface
	public interface AttributeSetter<R> {
		/**
		 * Puts a built-in attribute value.
		 *
		 * @param attrib attribute
		 * @param value value
		 * @param <T> value type
		 * @return this setter
		 */
		<T> AttributeSetter<R> put(RegistryEntryAttribute<R, T> attrib, T value);
	}
}
