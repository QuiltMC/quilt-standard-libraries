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

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.api.predicates.ClassInjectionPredicate;
import org.quiltmc.qsl.component.api.predicates.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.api.predicates.InheritedInjectionPredicate;

import java.util.Map;
import java.util.Optional;

public final class Components {
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

	public static <C extends Component, S> Optional<C> expose(ComponentType<C> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getContainer().expose(id.id())
					.map(id::cast)
					.map(Optional::orElseThrow); // If the casting fails something is wrong with the provided ComponentType. In that case we just throw.
		}

		return Optional.empty();
	}

	public static <S> Map<Identifier, Component> exposeAll(S obj) {
		return obj instanceof ComponentProvider provider ? provider.getContainer().exposeAll() : ImmutableMap.of();
	}
}
