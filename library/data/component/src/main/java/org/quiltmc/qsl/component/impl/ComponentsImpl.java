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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.component.api.*;
import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.predicate.DynamicInjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.injection.manager.cached.CachedInjectionManager;
import org.quiltmc.qsl.component.impl.injection.manager.dynamic.DynamicInjectionManager;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class ComponentsImpl {
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY =
			RegistryKey.ofRegistry(CommonInitializer.id("component_types"));
	public static final Registry<ComponentType<?>> REGISTRY = // TODO: Register this, maybe?!
			new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental(), null);

	public static final CachedInjectionManager CACHED_MANAGER = new CachedInjectionManager();
	public static final DynamicInjectionManager DYNAMIC_MANAGER = new DynamicInjectionManager();

	public static final Logger LOGGER = LoggerFactory.getLogger("QSL/Components");

	public static <C extends Component> void inject(InjectionPredicate predicate, ComponentEntry<C> componentEntry) {
		if (predicate instanceof DynamicInjectionPredicate dynamicPredicate) {
			DYNAMIC_MANAGER.inject(dynamicPredicate, componentEntry);
		} else {
			CACHED_MANAGER.inject(predicate, componentEntry);
		}
	}

	public static List<ComponentEntry<?>> getInjections(ComponentProvider provider) {
		var result = new ArrayList<ComponentEntry<?>>();
		result.addAll(CACHED_MANAGER.getInjections(provider));
		result.addAll(DYNAMIC_MANAGER.getInjections(provider));
		return result;
	}

	public static <C extends Component> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return Registry.register(REGISTRY, id, type);
	}

	public static void freezeRegistries() {
		REGISTRY.freeze();
		SyncPacketHeader.REGISTRY.freeze();
	}
}
