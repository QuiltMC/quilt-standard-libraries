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
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A global identifier for a specific type of {@linkplain Component components}.<br/>
 * This class <b>must always exist as singleton instances, which have to be registered under the {@linkplain Components#REGISTRY registry}</b>.
 *
 * @param id The {@link Identifier} the type is registered with(it's here just for accesibility, however you may also get access to it using the {@linkplain Components#REGISTRY registry} and a {@link ComponentType} instance).
 * @param defaultFactory A factory to a default {@link Component} instance, that this type may produce,
 *                       if no specific factory is defined under the container {@link org.quiltmc.qsl.component.impl.injection.ComponentEntry}.<br/>
 *                       Used only during component injections or creation of {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer containers}.
 * @param isStatic Whether this {@linkplain ComponentType type} works as a static type.
 * @param isInstant Whether this {@linkplain ComponentType type} will be instantly initialized when put in a container. Useful for {@link org.quiltmc.qsl.component.api.component.TickingComponent}s or componens you want to initialize on {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer container} creation.
 * @param <T> The generic type of {@link Component} this type will provide. Most of the time, you would want this to be an interface providing all the needed API you may use to interact with a {@link Component} of this {@linkplain ComponentType type}.
 * @author 0xJoeMama
 */
public record ComponentType<T extends Component>(Identifier id, Component.Factory<T> defaultFactory,
												 boolean isStatic, boolean isInstant) implements Component.Factory<T> {
	/**
	 * We provide a {@link NetworkCodec} to be used with manual registry sync, until the registry sync API is merged.
	 */
	public static final NetworkCodec<ComponentType<?>> NETWORK_CODEC =
			NetworkCodec.VAR_INT.map(Components.REGISTRY::getRawId, rawId -> ClientSyncHandler.getInstance().getType(rawId));
	/**
	 * @see Static
	 */
	private static final Static STATIC_CACHE = new Static();

	/**
	 * Performed an unchecked unsafe cast on the provided component.<br/>
	 * Used to attempt to translate any {@linkplain Component normal component} into a specific type.
	 *
	 * @param component The {@link Component} the cast is to be performed on.
	 * @return Instead of letting the {@link ClassCastException} be thrown on invalid casts, we catch it and return {@link Maybe#nothing()} if the component cannot be cast.
	 * Otherwise, we just wrap the cast result into a {@link Maybe}.
	 */
	@SuppressWarnings("unchecked")
	public Maybe<T> cast(Component component) {
		try {
			return Maybe.just((T) component);
		} catch (ClassCastException ignored) {
			return Maybe.nothing();
		}
	}

	/**
	 * A {@linkplain ComponentType type} functions as its own {@link Component.Factory} using the provided {@link ComponentType#defaultFactory}.
	 *
	 * @param operations The {@link Component.Operations} that the {@link Component} may use.
	 * @return A {@link Component} of type {@link T}.
	 */
	@Override
	public T create(Component.Operations operations) {
		if (this.isStatic) { // First check for static components
			return STATIC_CACHE.getOrCreate(this, operations);
		}

		// Otherwise just create a new one.
		return this.defaultFactory.create(operations);
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
		 * We store singleton instances in an {@link IdentityHashMap} as is common practice when using {@link ComponentType}s as keys.
		 */
		private final Map<ComponentType<?>, Component> staticInstances = new IdentityHashMap<>();

		private Static() { }

		/**
		 * Performs simple lazy initialization on the provided {@link ComponentType static type}
		 * using the provided {@link org.quiltmc.qsl.component.api.Component.Operations} as initialization arguments.
		 * @param type The type of the component to be created.
		 * @param operations The operations to use as initialization arguments.
		 * @return A singleton {@link C} instance.
		 * @param <C> The type of {@linkplain Component component} to be created.
		 */
		@SuppressWarnings("unchecked")
		public <C extends Component> C getOrCreate(ComponentType<C> type, Component.Operations operations) {
			if (this.staticInstances.containsKey(type)) {
				return (C) this.staticInstances.get(type);
			} else {
				C singleton = type.defaultFactory.create(operations);
				this.staticInstances.put(type, singleton);
				return singleton;
			}
		}
	}
}
