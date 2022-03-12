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
import org.quiltmc.qsl.registry.dict.impl.RegistryDictHolder;

/**
 * Extensions for working with {@link Registry}s.
 */
public final class RegistryExtensions {
	/**
	 * Registers an entry into a registry, also setting built-in dictionary values for it.
	 *
	 * @param registry        target registry
	 * @param id              entry identifier
	 * @param toRegister      entry to register
	 * @param builderConsumer builder consumer
	 * @param <R>             type of the entries in the registry
	 * @param <T>             type of the entry we're currently registering (may be a subclass of {@code R})
	 * @return the newly registered entry
	 */
	public static <R, T extends R> T register(Registry<R> registry, Identifier id, T toRegister,
											  Consumer<DefaultDictBuilder<R>> builderConsumer) {
		Registry.register(registry, id, toRegister);
		builderConsumer.accept(new DefaultDictBuilderImpl<>(registry, toRegister));
		return toRegister;
	}

	/**
	 * Used to set default built-in dictionary values in a builder-like fashion.
	 *
	 * @param <R> type of the entries in the registry
	 */
	@FunctionalInterface
	public interface DefaultDictBuilder<R> {
		/**
		 * Puts a built-in dictionary value.
		 *
		 * @param dict dictionary
		 * @param value  value to attach
		 * @param <V>    dictionaries' attached value type
		 * @return this setter
		 */
		<V> DefaultDictBuilder<R> put(RegistryDict<R, V> dict, V value);
	}

	public static <R, T extends R, V1> T register(Registry<R> registry, Identifier id, T toRegister,
												  RegistryDict<R, V1> dict1, V1 value1) {
		Registry.register(registry, id, toRegister);
		var holder = RegistryDictHolder.getBuiltin(registry);
		holder.putValue(dict1, toRegister, value1);
		return toRegister;
	}

	public static <R, T extends R, V1, V2> T register(Registry<R> registry, Identifier id, T toRegister,
													  RegistryDict<R, V1> dict1, V1 value1,
													  RegistryDict<R, V2> dict2, V2 value2) {
		Registry.register(registry, id, toRegister);
		var holder = RegistryDictHolder.getBuiltin(registry);
		holder.putValue(dict1, toRegister, value1);
		holder.putValue(dict2, toRegister, value2);
		return toRegister;
	}

	public static <R, T extends R, V1, V2, V3> T register(Registry<R> registry, Identifier id, T toRegister,
														  RegistryDict<R, V1> dict1, V1 value1,
														  RegistryDict<R, V2> dict2, V2 value2,
														  RegistryDict<R, V3> dict3, V3 value3) {
		Registry.register(registry, id, toRegister);
		var holder = RegistryDictHolder.getBuiltin(registry);
		holder.putValue(dict1, toRegister, value1);
		holder.putValue(dict2, toRegister, value2);
		holder.putValue(dict3, toRegister, value3);
		return toRegister;
	}

	public static <R, T extends R, V1, V2, V3, V4> T register(Registry<R> registry, Identifier id, T toRegister,
															  RegistryDict<R, V1> dict1, V1 value1,
															  RegistryDict<R, V2> dict2, V2 value2,
															  RegistryDict<R, V3> dict3, V3 value3,
															  RegistryDict<R, V4> dict4, V4 value4) {
		Registry.register(registry, id, toRegister);
		var holder = RegistryDictHolder.getBuiltin(registry);
		holder.putValue(dict1, toRegister, value1);
		holder.putValue(dict2, toRegister, value2);
		holder.putValue(dict3, toRegister, value3);
		holder.putValue(dict4, toRegister, value4);
		return toRegister;
	}
}
