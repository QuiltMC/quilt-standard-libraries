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
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.component.TickingComponent;
import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
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

/**
 * The external version of the component API, meant to be used by modders.<br/>
 * Use this and not {@link ComponentsImpl}.<br/>
 *
 * For usage docs, check {@link package-info.java}
 *
 * @author 0xJoeMama
 */
@ApiStatus.Experimental
public final class Components {
	// begin registry
	/**
	 * The registry used for {@link ComponentType}s.
	 */
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY = ComponentsImpl.REGISTRY_KEY;
	public static final Registry<ComponentType<?>> REGISTRY = ComponentsImpl.REGISTRY;
	// end registry

	// begin injection methods
	/**
	 * The most manual method of injection.<br/>
	 * Callers can provide custom {@link InjectionPredicate}s and also custom {@linkplain ComponentEntry entries}
	 * @param predicate The {@linkplain InjectionPredicate predicate} used for the injection
	 * @param entry The {@linkplain ComponentEntry entry} used for the injection.
	 * @param <C> The type of the injected component.
	 * @see InjectionPredicate
	 * @see ComponentEntry
	 */
	public static <C extends Component> void inject(InjectionPredicate predicate, ComponentEntry<C> entry) {
		ComponentsImpl.inject(predicate, entry);
	}

	/**
	 * The simplest method of injection.<br/>
	 * Injects the provided {@link ComponentType} into all <i>direct</i> instances of the provided {@link Class}.
	 *
	 * @param clazz The target class(must implement {@link ComponentProvider}).
	 * @param type The type to inject(the {@linkplain ComponentType#defaultFactory() default factory of the type} is used).
	 * @param <C> The type held by the injected type.
	 * @see ClassInjectionPredicate
	 */
	public static <C extends Component> void inject(Class<?> clazz, ComponentType<C> type) {
		ComponentsImpl.inject(new ClassInjectionPredicate(clazz), new ComponentEntry<>(type));
	}

	/**
	 * Similar to {@link Components#inject(Class, ComponentType)} except it injects into all subclasses and indirect instances of the provided class.
	 * @param clazz The highest class in the hierarchy that will be injected.
	 * @param type The type to inject(the {@linkplain ComponentType#defaultFactory() default factory of the type} is used).
	 * @param <C> The type held by the injected type.
	 * @see InheritedInjectionPredicate
	 */
	public static <C extends Component> void injectInheritage(Class<?> clazz, ComponentType<C> type) {
		ComponentsImpl.inject(new InheritedInjectionPredicate(clazz), new ComponentEntry<>(type));
	}

	/**
	 * Similar to {@link Components#injectInheritage(Class, ComponentType)}, except it skips the provided classes in the hierarchy.
	 *
	 * @param clazz The highest class in the hierarchy that will be injected.
	 * @param type The type to inject(the {@linkplain ComponentType#defaultFactory() default factory of the type} is used).
	 * @param exceptions The classes to avoid injecting into.
	 * @param <C> The type help by the provided type.
	 * @see FilteredInheritedInjectionPredicate
	 */
	public static <C extends Component> void injectInheritanceExcept(Class<?> clazz, ComponentType<C> type, Class<?>... exceptions) {
		ComponentsImpl.inject(new FilteredInheritedInjectionPredicate(clazz, exceptions), new ComponentEntry<>(type));
	}

	/**
	 * TODO: This is supposed to be merged with {@link Components#inject(Class, ComponentType)} and a mapping to redirections is to be created.
	 */
	public static <C extends Component> void injectRedirected(Class<?> mainClass, ComponentType<C> type, Class<?>... others) {
		ComponentsImpl.inject(new RedirectedInjectionPredicate(mainClass, Set.of(others)), new ComponentEntry<>(type));
	}

	/**
	 * Dynamically injects into the provided {@link Class} using the provided predicate.<br/>
	 * For more info on dynamic injection check {@link org.quiltmc.qsl.component.api.predicate.DynamicInjectionPredicate};
	 *
	 * @param clazz The target class(must implement {@link ComponentProvider}).
	 * @param type The type to inject(the {@linkplain ComponentType#defaultFactory() default factory of the type} is used).
	 * @param predicate The predicate used to determine if injection is possible.
	 * @param <C> The type of component that will be injected.
	 * @param <P> The type of the provider that the dynamic injection targets.
	 */
	public static <C extends Component, P extends ComponentProvider> void injectDynamic(Class<P> clazz, ComponentType<C> type, Predicate<P> predicate) {
		ComponentsImpl.inject(new DynamicClassInjectionPredicate<>(clazz, predicate), new ComponentEntry<>(type));
	}
	// end injection methods

	// begin registration methods
	/**
	 * The proper way to expose component instances.<br/>
	 * This does type-checking and also makes sure a {@link ComponentProvider} is given.<br/>
	 * This should be used where interface injection cannot apply(in other words custom implementations of {@link ComponentProvider}).<br/>
	 *
	 * @param id The {@link ComponentType} to expose.
	 * @param obj Any object will work. If it does not implement {@link ComponentProvider} an empty will be instantly returned.
	 * @return Either {@link org.quiltmc.qsl.base.api.util.Maybe.Just} the result of expose called on the provider's container
	 *		   or {@link org.quiltmc.qsl.base.api.util.Maybe.Nothing} if the provided object is not a {@link ComponentProvider}.
	 * @param <C> The type of component held by the {@link ComponentType}.
	 * @param <S> The object to attempt to expose the component on.
	 * @see ComponentProvider#expose
	 * @see org.quiltmc.qsl.component.api.container.ComponentContainer#expose
	 */
	public static <C extends Component, S> Maybe<C> expose(ComponentType<C> id, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getComponentContainer().expose(id)
							   .filterMap(id::cast); // mapping with cast will make sure we get the correct type back.
		}

		return Maybe.nothing();
	}

	/**
	 * Register a manually created {@link ComponentType}.
	 *
	 * @param id The id to register the type with.
	 * @param type The type to register.
	 * @return The registered version of the component type.
	 * @param <C> The type of component held by the type.
	 */
	public static <C extends Component> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return ComponentsImpl.register(id, type);
	}

	/**
	 * Register a normal {@link ComponentType} using the provided {@linkplain Component.Factory default factory}.
	 *
	 * @param id The id to register the type with.
	 * @param factory The default {@link Component.Factory} of the type.
	 * @return A new {@link ComponentType} of {@link C} components.
	 * @param <C> The type of component held by the type.
	 */
	public static <C extends Component> ComponentType<C> register(Identifier id, Component.Factory<C> factory) {
		if (factory instanceof ComponentType<C>) {
			throw ErrorUtil.illegalArgument("Do NOT register ComponentTypes as factories, use the correct method").get();
		}
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, false));
	}

	/**
	 * Register a {@linkplain ComponentType.Static static} {@linkplain ComponentType type} using the provided factory.
	 * @param id THe id to register the type with.
	 * @param factory The default {@link Component.Factory} of the type.
	 * @return A new static {@link ComponentType}.
	 * @param <C> The type of component held by the type.
	 */
	public static <C extends Component> ComponentType<C> registerStatic(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, true, false));
	}

	/**
	 * Register an instance {@link ComponentType}.<br/>
	 * For more info check the {@linkplain ComponentType relevant docs}.
	 *
	 * @param id The id to register the type with.
	 * @param factory The default {@link Component.Factory} of the type.
	 * @return A new instant {@link ComponentType}.
	 * @param <C> The type of component held by the type.
	 */
	public static <C extends TickingComponent> ComponentType<C> registerInstant(Identifier id, Component.Factory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, true));
	}
	// end registration method
}
