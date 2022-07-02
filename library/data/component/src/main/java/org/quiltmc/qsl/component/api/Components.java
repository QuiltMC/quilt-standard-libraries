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
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.predicates.ClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.InheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.predicates.RedirectedInjectionPredicate;

import java.util.Optional;
import java.util.Set;

public final class Components {
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY = ComponentsImpl.REGISTRY_KEY;

	public static final Registry<ComponentType<?>> REGISTRY = ComponentsImpl.REGISTRY;

	public static <C extends Component> void inject(ComponentInjectionPredicate predicate, ComponentType<C> component) {
		ComponentsImpl.inject(predicate, component);
	}

	public static <C extends Component> void inject(Class<?> clazz, ComponentType<C> component) {
		ComponentsImpl.inject(new ClassInjectionPredicate(clazz), component);
	}

	public static <C extends Component> void injectInheritage(Class<?> clazz, ComponentType<C> component) {
		ComponentsImpl.inject(new InheritedInjectionPredicate(clazz), component);
	}

	public static <C extends Component> void injectInheritanceExcept(Class<?> clazz, ComponentType<C> component, Class<?>... exceptions) {
		ComponentsImpl.inject(new FilteredInheritedInjectionPredicate(clazz, exceptions), component);
	}

	public static <C extends Component> void injectRedirected(Class<?> mainClass, ComponentType<C> type, Class<?> others) {
		ComponentsImpl.inject(new RedirectedInjectionPredicate(mainClass, Set.of(others)), type);
	}

	public static <C extends Component, S> Optional<C> expose(ComponentType<C> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getComponentContainer().expose(id).flatMap(id::cast);
		}

		return Optional.empty();
	}

	public static <C extends Component> ComponentType<C> register(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, false));
	}

	public static <C extends Component> ComponentType<C> registerStatic(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, true, false));
	}

	public static <C extends TickingComponent> ComponentType<C> registerTicking(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, true));
	}
}
