/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.quiltmc.qsl.component.api.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ComponentCache {

	private static ComponentCache INSTANCE;

	private final Map<Class<?>, Map<Identifier, Supplier<? extends Component>>> injectionCache = new HashMap<>();

	private ComponentCache() {

	}

	public static ComponentCache getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ComponentCache();
		}

		return INSTANCE;
	}

	public Optional<Map<Identifier, Supplier<? extends Component>>> getCache(Class<?> clazz) {
		if (!this.injectionCache.containsKey(clazz)) {
			return Optional.empty();
		}

		return Optional.of(this.injectionCache.get(clazz));
	}

	public void clear() {
		this.injectionCache.clear();
	}

	public void record(Class<?> clazz, Map<Identifier, Supplier<? extends Component>> components) {
		if (this.injectionCache.put(clazz, Util.make(new HashMap<>(), map -> map.putAll(components))) != null) {
			// If there was a value there, it means we attempted an override, and so we throw.
			throw new IllegalStateException("Cannot register cache twice for class %s".formatted(clazz));
		}
	}
}
