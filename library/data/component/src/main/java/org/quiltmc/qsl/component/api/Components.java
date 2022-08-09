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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.injection.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

/**
 * The external version of the component API, meant to be used by modders.<br/>
 * Use this and not {@link ComponentsImpl}.<br/>
 *
 * <p>
 * For usage docs, check package-info.java
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
	 * Callers can provide custom {@link InjectionPredicate}s and also custom {@linkplain ComponentEntry entries}.
	 *
	 * <p>
	 * For users who wish to use an easier method of injection, see
	 * {@link org.quiltmc.qsl.component.api.injection.ComponentInjector}.
	 *
	 * @param predicate The {@linkplain InjectionPredicate predicate} used for the injection
	 * @param entries   The {@linkplain ComponentEntry entries} that will be injected.
	 * @see InjectionPredicate
	 * @see ComponentEntry
	 */
	public static void inject(InjectionPredicate predicate, ComponentEntry<?>... entries) {
		ComponentsImpl.inject(predicate, entries);
	}

	// end injection methods

	/**
	 * The proper way to expose component instances.<br/>
	 * This does type-checking and also makes sure a {@link ComponentProvider} is given.<br/>
	 * This should be used where interface injection cannot apply(in other words custom implementations of
	 * {@link ComponentProvider}).<br/>
	 *
	 * @param type The {@link ComponentType} to expose.
	 * @param obj  Any object will work. If it does not implement {@link ComponentProvider} an empty will be instantly
	 *             returned.
	 * @param <C>  The type of component held by the {@link ComponentType}.
	 * @param <S>  The object to attempt to expose the component on.
	 * @return Either the result of expose called on the provider's container
	 * or null if the provided object is not a
	 * {@link ComponentProvider}.
	 * @see ComponentProvider#expose
	 * @see org.quiltmc.qsl.component.api.container.ComponentContainer#expose
	 */
	@Nullable
	public static <C, S> C expose(ComponentType<C> type, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getComponentContainer().expose(type);
		}

		return null;
	}

	@Nullable
	public static <C, S> C ifPresent(S obj, ComponentType<C> type, Consumer<? super C> action) {
		if (obj instanceof ComponentProvider provider) {
			return provider.ifPresent(type, action);
		}

		return null;
	}

	// begin registration methods

	/**
	 * Register a manually created {@link ComponentType}.
	 *
	 * @param id   The id to register the type with.
	 * @param type The type to register.
	 * @param <C>  The type of component held by the type.
	 * @return The registered version of the component type.
	 */
	public static <C> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return ComponentsImpl.register(id, type);
	}

	/**
	 * Register a normal {@link ComponentType} using the provided {@linkplain ComponentFactory default factory}.
	 *
	 * @param id      The id to register the type with.
	 * @param factory The default {@link ComponentFactory} of the type.
	 * @param <C>     The type of component held by the type.
	 * @return A new {@link ComponentType} of {@link C} components.
	 */
	public static <C> ComponentType<C> register(Identifier id, ComponentFactory<C> factory) {
		if (factory instanceof ComponentType<C>) {
			throw ErrorUtil.illegalArgument("Do NOT register ComponentTypes as factories, use the correct method")
						   .get();
		}

		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, false));
	}

	/**
	 * Register a {@linkplain ComponentType.Static static} {@linkplain ComponentType type} using the provided factory.
	 *
	 * @param id      THe id to register the type with.
	 * @param factory The default {@link ComponentFactory} of the type.
	 * @param <C>     The type of component held by the type.
	 * @return A new static {@link ComponentType}.
	 */
	public static <C> ComponentType<C> registerStatic(Identifier id, ComponentFactory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, true, false));
	}

	/**
	 * Register an instance {@link ComponentType}.<br/>
	 * For more info check the {@linkplain ComponentType relevant docs}.
	 *
	 * @param id      The id to register the type with.
	 * @param factory The default {@link ComponentFactory} of the type.
	 * @param <C>     The type of component held by the type.
	 * @return A new instant {@link ComponentType}.
	 */
	public static <C extends Tickable> ComponentType<C> registerInstant(Identifier id, ComponentFactory<C> factory) {
		return ComponentsImpl.register(id, new ComponentType<>(id, factory, false, true));
	}

	// end registration method
}
