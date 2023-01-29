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

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.injection.predicate.DynamicInjectionPredicate;
import org.quiltmc.qsl.component.api.injection.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.manager.cached.CachedInjectionManager;
import org.quiltmc.qsl.component.impl.injection.manager.dynamic.DynamicInjectionManager;

@ApiStatus.Internal
public class ComponentsImpl {
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY =
			RegistryKey.ofRegistry(CommonInitializer.id("component_types"));
	public static final Registry<ComponentType<?>> REGISTRY = // TODO: Register this, maybe?!
			new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental(), null);

	public static final CachedInjectionManager CACHED_MANAGER = new CachedInjectionManager();
	public static final DynamicInjectionManager DYNAMIC_MANAGER = new DynamicInjectionManager();

	public static final Logger LOGGER = LoggerFactory.getLogger("Quilt Component");
	public static final ComponentContainer.Factory<?> DEFAULT_FACTORY = (provider, entries, saveOperation, ticking, syncChannel) ->
			entries.isEmpty() ?
			ComponentContainer.EMPTY :
			ComponentContainer.LAZY_FACTORY.generate(provider, entries, saveOperation, ticking, syncChannel);

	public static void inject(InjectionPredicate predicate, ComponentEntry<?>... entries) {
		if (predicate instanceof DynamicInjectionPredicate dynamicPredicate) {
			DYNAMIC_MANAGER.inject(dynamicPredicate, entries);
		} else {
			CACHED_MANAGER.inject(predicate, entries);
		}
	}

	public static List<ComponentEntry<?>> getInjections(ComponentProvider provider) {
		var result = new ArrayList<ComponentEntry<?>>();
		result.addAll(CACHED_MANAGER.getInjections(provider));
		result.addAll(DYNAMIC_MANAGER.getInjections(provider));
		return List.copyOf(result);
	}

	public static <C> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return Registry.register(REGISTRY, id, type);
	}
}
