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

	private static ComponentInjectionCache INSTANCE;

	private final Map<Class<?>, List<ComponentType<?>>> injectionCache = new HashMap<>();

	private ComponentInjectionCache() {

	}

	public static ComponentInjectionCache getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ComponentInjectionCache();
		}

		return INSTANCE;
	}

	public Optional<List<ComponentType<?>>> getCache(Class<?> clazz) {
		if (!this.injectionCache.containsKey(clazz)) {
			return Optional.empty();
		}

		return Optional.of(this.injectionCache.get(clazz));
	}

	public void clear() {
		this.injectionCache.clear();
	}

	public void record(Class<?> clazz, List<ComponentType<?>> components) {
		if (this.injectionCache.put(clazz, Util.make(new ArrayList<>(), list -> list.addAll(components))) != null) {
			// If there was a value there, it means we attempted an override, and so we throw.
			throw new IllegalStateException("Cannot register cache twice for class %s".formatted(clazz));
		}
	}
}
