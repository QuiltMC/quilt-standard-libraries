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

package org.quiltmc.qsl.component.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.ClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.InheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.RedirectedInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.dynamic.DynamicClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

import java.util.Set;
import java.util.function.Predicate;

public final class Components {
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY = ComponentsImpl.REGISTRY_KEY;
	public static final Registry<ComponentType<?>> REGISTRY = ComponentsImpl.REGISTRY;

	public static <C extends Component> void inject(CachedInjectionPredicate predicate, ComponentType<C> type) {
		ComponentsImpl.inject(predicate, new ComponentEntry<>(type));
	}

	public static <C extends Component> void inject(Class<?> clazz, ComponentType<C> type) {
		ComponentsImpl.inject(new ClassInjectionPredicate(clazz), new ComponentEntry<>(type));
	}

	public static <C extends Component> void injectInheritage(Class<?> clazz, ComponentType<C> type) {
		ComponentsImpl.inject(new InheritedInjectionPredicate(clazz), new ComponentEntry<>(type));
	}

	public static <C extends Component> void injectInheritanceExcept(Class<?> clazz, ComponentType<C> type, Class<?>... exceptions) {
		ComponentsImpl.inject(new FilteredInheritedInjectionPredicate(clazz, exceptions), new ComponentEntry<>(type));
	}

	public static <C extends Component> void injectRedirected(Class<?> mainClass, ComponentType<C> type, Class<?>... others) {
		ComponentsImpl.inject(new RedirectedInjectionPredicate(mainClass, Set.of(others)), new ComponentEntry<>(type));
	}

	public static <C extends Component, P extends ComponentProvider> void injectDynamic(Class<P> clazz, ComponentType<C> type, Predicate<P> predicate) {
		// TODO: Fix evil hack if possible
		ComponentsImpl.inject(new DynamicClassInjectionPredicate<>(clazz, predicate), new ComponentEntry<>(type));
	}

	public static <C extends Component, S> Maybe<C> expose(ComponentType<C> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getComponentContainer().expose(id).filterMap(id::cast);
		}

		return Maybe.nothing();
	}

	public static <C extends Component> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return ComponentsImpl.register(id, type);
	}

	public static <C extends Component> ComponentType<C> register(Identifier id, Component.Factory<C> factory) {
		if (factory instanceof ComponentType<C>) {
			throw ErrorUtil.illegalArgument("Do NOT register ComponentTypes as factories, use the correct method").get();
		}
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, false));
	}

	public static <C extends Component> ComponentType<C> registerStatic(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, true, false));
	}

	public static <C extends TickingComponent> ComponentType<C> registerTicking(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, true));
	}
}
