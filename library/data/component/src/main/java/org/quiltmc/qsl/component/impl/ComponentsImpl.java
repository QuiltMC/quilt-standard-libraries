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
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentInjectionPredicate;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.impl.sync.header.SyncHeaderRegistry;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ComponentsImpl {
	// TODO: Maybe move this to the ComponentType class?!
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY =
			RegistryKey.ofRegistry(CommonInitializer.id("component_types"));
	public static final Registry<ComponentType<?>> REGISTRY =
			new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental(), null);

	public static final Logger LOGGER = LoggerFactory.getLogger("QSL/Components");

	private static final Map<ComponentInjectionPredicate, Set<ComponentType<?>>> INJECTION_REGISTRY = new HashMap<>();

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentType<C> type) {
		if (REGISTRY.get(type.id()) == null) {
			throw ErrorUtil.illegalArgument("The target id %s does not match any registered component", type).get();
		}

		if (INJECTION_REGISTRY.containsKey(predicate)) {
			if (!INJECTION_REGISTRY.get(predicate).add(type)) {
				throw ErrorUtil.illegalArgument(
						"Cannot inject the predicate %s with id %s more than once! Consider creating a new component type!",
						predicate, type.id()
				).get();
			}
		} else {
			INJECTION_REGISTRY.put(predicate, Util.make(new HashSet<>(), set -> set.add(type)));
		}

		ComponentInjectionCache.clear(); // Always clear the cache after an injection is registered.
	}

	@NotNull
	public static Set<ComponentType<?>> getInjections(@NotNull ComponentProvider provider) {
		return ComponentInjectionCache.getCache(provider.getClass()).orElseGet(() -> {
			Set<ComponentType<?>> injectedTypes = INJECTION_REGISTRY.entrySet().stream()
					.filter(it -> it.getKey().canInject(provider))
					.map(Map.Entry::getValue)
					.flatMap(Set::stream)
					.collect(Collectors.toSet());

			ComponentInjectionCache.record(provider.getClass(), injectedTypes);

			return injectedTypes;
		});
	}

	@NotNull
	public static <C extends Component> ComponentType<C> register(Identifier id, @NotNull ComponentType<C> type) {
		return Registry.register(REGISTRY, id, type);
	}

	public static void freezeRegistries() {
		REGISTRY.freeze();
		SyncHeaderRegistry.HEADERS.freeze();
	}
}
