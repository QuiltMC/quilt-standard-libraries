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
import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.ComponentIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComponentsImpl {
	private static final Map<ComponentInjectionPredicate, Set<Identifier>> INJECTION_REGISTRY = new HashMap<>();
	private static final Map<Identifier, Component.Factory<?>> REGISTRY = new HashMap<>(); // TODO: Look into using a Minecraft Registry for this.

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentIdentifier<C> id) {
		if (REGISTRY.get(id.id()) == null) {
			throw new IllegalArgumentException(
					"The target id %s does not match any registered component!".formatted(id.toString())
			);
		}

		if (INJECTION_REGISTRY.containsKey(predicate)) {
			if (!INJECTION_REGISTRY.get(predicate).add(id.id())) {
				throw new IllegalStateException(
						"Cannot inject the predicate %s with %s more than once! Consider creating a new component!"
								.formatted(predicate.toString(), id.id().toString())
				);
			}
		} else {
			INJECTION_REGISTRY.put(predicate, Util.make(new HashSet<>(), set -> set.add(id.id())));
		}

		ComponentCache.getInstance().clear(); // Always clear the cache after an injection is registered.
	}

	public static <T extends Component> ComponentIdentifier<T> register(Identifier id, Component.Factory<T> component) {
		REGISTRY.put(id, component);
		return new ComponentIdentifier<>(id);
	}

	public static Map<Identifier, Component.Factory<?>> get(ComponentProvider provider) {
		return ComponentCache.getInstance().getCache(provider.getClass()).orElseGet(() -> {
			Map<Identifier, Component.Factory<?>> returnMap = INJECTION_REGISTRY.entrySet().stream()
					.filter(it -> it.getKey().canInject(provider))
					.map(Map.Entry::getValue)
					.collect(HashMap::new, (map, ids) -> ids.forEach(id -> map.put(id, REGISTRY.get(id))), HashMap::putAll);

			ComponentCache.getInstance().record(provider.getClass(), returnMap);

			return returnMap;
		});
	}

	public static Component.Factory<?> getEntry(Identifier id) {
		return REGISTRY.get(id);
	}
}
