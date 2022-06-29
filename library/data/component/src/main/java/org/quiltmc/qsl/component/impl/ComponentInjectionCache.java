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

import net.minecraft.util.Util;
import org.quiltmc.qsl.component.api.ComponentType;

import java.util.*;

public class ComponentInjectionCache {
	private static final Map<Class<?>, List<ComponentType<?>>> INJECTION_CACHE = new HashMap<>();

	private ComponentInjectionCache() {

	}

	public static Optional<List<ComponentType<?>>> getCache(Class<?> clazz) {
		if (!INJECTION_CACHE.containsKey(clazz)) {
			return Optional.empty();
		}

		return Optional.of(INJECTION_CACHE.get(clazz));
	}

	public static void clear() {
		INJECTION_CACHE.clear();
	}

	public static void record(Class<?> clazz, List<ComponentType<?>> components) {
		if (INJECTION_CACHE.put(clazz, Util.make(new ArrayList<>(), list -> list.addAll(components))) != null) {
			// If there was a value there, it means we attempted an override, and so we throw.
			throw new IllegalStateException("Cannot register cache twice for class %s".formatted(clazz));
		}
	}
}
