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

package org.quiltmc.qsl.registry.dict.api;

import java.util.function.Consumer;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.impl.DefaultDictBuilderImpl;

/**
 * Extensions for working with {@link Registry}s.
 */
public final class RegistryExtensions {
	/**
	 * Registers an entry into a registry, also setting built-in attribute values for it.
	 *
	 * @param registry        target registry
	 * @param id              entry identifier
	 * @param toRegister      entry to register
	 * @param builderConsumer builder consumer
	 * @param <R>             type of the entries in the registry
	 * @param <T>             type of the entry we're currently registering (may be a subclass of {@code R})
	 * @return the newly registered entry
	 */
	public static <R, T extends R> T registerWithDictValues(Registry<R> registry, Identifier id, T toRegister,
															Consumer<DefaultDictBuilder<R>> builderConsumer) {
		Registry.register(registry, id, toRegister);
		builderConsumer.accept(new DefaultDictBuilderImpl<>(registry, toRegister));
		return toRegister;
	}

	/**
	 * Used to set default built-in attribute values in a builder-like fashion.
	 *
	 * @param <R> type of the entries in the registry
	 */
	@FunctionalInterface
	public interface DefaultDictBuilder<R> {
		/**
		 * Puts a built-in attribute value.
		 *
		 * @param dict attribute
		 * @param value  value to attach
		 * @param <V>    attributes' attached value type
		 * @return this setter
		 */
		<V> DefaultDictBuilder<R> put(RegistryDict<R, V> dict, V value);
	}
}
