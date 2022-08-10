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

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;

/**
 * A global identifier for a specific type of component.<br/>
 * This class <b>must always exist as singleton instances, which have to be registered under the
 * {@linkplain Components#REGISTRY registry}</b>.
 *
 * @param id             The {@link Identifier} the type is registered with(it's here just for accesibility, however
 *                       you may also get access to it using the {@linkplain Components#REGISTRY registry} and a
 *                       {@link ComponentType} instance).
 * @param defaultFactory A default factory for an instance of {@link T} instance, that this type may produce,
 *                       if no specific factory is defined under the container
 *                       {@link ComponentEntry}.<br/>
 *                       Used only during component injections or creation of
 *                       {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer containers}.
 * @param isStatic       Whether this {@linkplain ComponentType type} works as a static type.
 * @param isInstant      Whether this {@linkplain ComponentType type} will be instantly initialized when put in a
 *                       container. Useful for {@link Tickable}s or componens you want to initialize on
 *                       {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer container} creation.
 * @param <T>            The generic type of component this type will provide. Most of the time, you would
 *                       want this to be an interface providing all the needed API you may use to interact with a
 *                       component of this {@linkplain ComponentType type}.
 * @author 0xJoeMama
 */
public record ComponentType<T>(Identifier id, ComponentFactory<T> defaultFactory,
							   boolean isStatic, boolean isInstant) implements ComponentFactory<T> {
	/**
	 * We provide a {@link NetworkCodec} to be used with manual registry sync, until the registry sync API is merged.
	 */
	public static final NetworkCodec<ComponentType<?>> NETWORK_CODEC = NetworkCodec.VAR_INT.map(
			Components.REGISTRY::getRawId,
			rawId -> ClientSyncHandler.getInstance().getType(rawId)
	);
	/**
	 * @see Static
	 */
	private static final Static STATIC_CACHE = new Static();

	/**
	 * A {@linkplain ComponentType type} functions as its own {@link ComponentFactory} using the provided
	 * {@link ComponentType#defaultFactory}.
	 *
	 * @param ctx The {@link ComponentCreationContext} that the component can use.
	 * @return A component of type {@link T}.
	 */
	@Override
	public T create(ComponentCreationContext ctx) {
		if (this.isStatic) { // First check for static components
			return STATIC_CACHE.getOrCreate(this, ctx);
		}

		// Otherwise just create a new one.
		return this.defaultFactory.create(ctx);
	}

	/**
	 * Static components are singleton components, created only once during runtime.<br/>
	 * <i>Most of the time this is useful for components that only provide <b>behavior</b></i>.<br/>
	 * Other use-cases may be quite rare.<br/>
	 *
	 * @author 0xJoeMama
	 * @see ComponentType
	 */
	public static class Static {
		/**
		 * We store singleton instances in an {@link IdentityHashMap} as is common practice when using
		 * {@link ComponentType}s as keys.
		 */
		private final Map<ComponentType<?>, Object> staticInstances = new IdentityHashMap<>();

		private Static() { }

		/**
		 * Performs simple lazy initialization on the provided {@link ComponentType static type}
		 * using the provided {@link ComponentCreationContext} as initialization arguments.
		 *
		 * @param type       The type of the component to be created.
		 * @param operations The operations to use as initialization arguments.
		 * @param <C>        The type of component to be created.
		 * @return A singleton {@link C} instance.
		 */
		@SuppressWarnings("unchecked")
		public <C> C getOrCreate(ComponentType<C> type, ComponentCreationContext operations) {
			if (this.staticInstances.containsKey(type)) {
				return (C) this.staticInstances.get(type);
			}

			C singleton = type.defaultFactory.create(operations);
			this.staticInstances.put(type, singleton);
			return singleton;
		}
	}
}
