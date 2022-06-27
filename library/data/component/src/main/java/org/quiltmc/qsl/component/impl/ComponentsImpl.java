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

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ComponentsImpl {
	private static final Map<ComponentInjectionPredicate, Set<Identifier>> INJECTION_REGISTRY = new HashMap<>();
	private static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY =
			RegistryKey.ofRegistry(new Identifier("quilt", "components"));
	public static final Registry<ComponentType<?>> REGISTRY =
			new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental(), null);

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentType<C> id) {
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

		ComponentInjectionCache.getInstance().clear(); // Always clear the cache after an injection is registered.
	}

	public static <T extends Component> ComponentType<T> register(Identifier id, Component.Factory<T> factory) {
		var componentId = new ComponentType<>(id, factory);
		return Registry.register(REGISTRY, id, componentId);
	}

	public static List<ComponentType<?>> getInjections(ComponentProvider provider) {
		return ComponentInjectionCache.getInstance().getCache(provider.getClass()).orElseGet(() -> {
			List<ComponentType<?>> injectedTypes = INJECTION_REGISTRY.entrySet().stream()
					.filter(it -> it.getKey().canInject(provider))
					.map(Map.Entry::getValue)
					.flatMap(Set::stream)
					.map(ComponentsImpl::getEntry)
					.collect(Collectors.toList());

			ComponentInjectionCache.getInstance().record(provider.getClass(), injectedTypes);

			return injectedTypes;
		});
	}

	public static ComponentType<?> getEntry(Identifier id) {
		return REGISTRY.getOrEmpty(id).orElseThrow(
				ErrorUtil.illegalArgument("Cannot access element with id %s in the component registry!".formatted(id))
		);
	}
}
