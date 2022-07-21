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

package org.quiltmc.qsl.component.api.container;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.*;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * A base container for all {@link Component}s that a {@link ComponentProvider} contains.
 * The implementation of this, needs to not take up too much runtime, since the methods in here may be called really often.
 * <p/>
 * There are default implementations used internally in the API implementation, but they are also usable outside of internals.
 * These include:
 * <ol>
 *     <li>{@link EmptyComponentContainer}: usable through the {@link ComponentContainer#EMPTY} instance or the {@link ComponentContainer#EMPTY_FACTORY} instance.</li>
 *     <li>{@link SimpleComponentContainer}: usable through the {@link ComponentContainer#SIMPLE_FACTORY} instance.</li>
 *     <li>{@link LazyComponentContainer}: usable through the {@link ComponentContainer#LAZY_FACTORY} instance.</li>
 *     <li>{@link OnAccessComponentContainer}: usable through the {@link ComponentContainer#ON_ACCESS_FACTORY} instance.</li>
 *     <li>{@link SingleComponentContainer}: usable through the {@link ComponentContainer#createSingleFactory} method.</li>
 *     <li>{@link CompositeComponentContainer}: usable through the {@link ComponentContainer#createComposite} method.</li>
 * </ol>
 *
 * To create a container, most of the time, you should use the {@link Builder} class.<br/>
 * You are also free to extend it and create a custom {@link Builder} implementation to suit your needs.
 *
 * @see ComponentProvider
 * @see ComponentType
 * @see Component
 * @author 0xJoeMama
 */
public interface ComponentContainer {
	/**
	 * A {@link ComponentContainer} that holds no components.
	 */
	ComponentContainer EMPTY = EmptyComponentContainer.INSTANCE;
	/**
	 * A {@link Factory} to the aforementioned {@link EmptyComponentContainer}.<br/>
	 * <b>This will always return the same instance.<b/>
	 */
	ComponentContainer.Factory<EmptyComponentContainer> EMPTY_FACTORY = EmptyComponentContainer.FACTORY;
	/**
	 * A {@link Factory} to a {@link SimpleComponentContainer}.<br/>
	 * A {@link SimpleComponentContainer} instantly initializes all of its components, disregarding the
	 * {@link ComponentType#isInstant()} value for their types.<br/>
	 *
	 * The advantage of this {@linkplain ComponentContainer container} is that by initializing all of its components right off the bat,
	 * it can have a faster runtime. However, that does sacrifice on memory and also on disc space,
	 * since all {@link org.quiltmc.qsl.component.api.component.NbtComponent}s, even if their value hasn't been modified, will be saved.
	 */
	ComponentContainer.Factory<SimpleComponentContainer> SIMPLE_FACTORY = SimpleComponentContainer.FACTORY;
	/**
	 * A {@link Factory} to a {@link LazyComponentContainer}.<br/>
	 * A {@link LazyComponentContainer} lazily initializes its components.
	 * This saves on memory and disc space, since non-initialized components, will not be written to disc.<br/>
	 * The runtime hit of using this {@linkplain ComponentContainer container} is minimal, since it mostly consists of work that would also be done at runtime by a {@link SimpleComponentContainer}.<br/>
	 * The only functional difference is that this container will pull injections targetting its provider.
	 * This container is used as the default for all type at the moment.
	 */
	ComponentContainer.Factory<LazyComponentContainer> LAZY_FACTORY = LazyComponentContainer.FACTORY;
	/**
	 * A {@link Factory} to a {@link OnAccessComponentContainer}.<br/>
	 * An {@link OnAccessComponentContainer} is similar to a {@link LazyComponentContainer} in that it lazily initializes its components.
	 * However, it does so in a different way. Instead of storing {@link Lazy} instances,
	 * it stores {@link com.mojang.datafixers.util.Either} instances that it attempts to access and/or initialize on expose.
	 * This currently leads to extra object creation, hence it's considered {@linkplain org.jetbrains.annotations.ApiStatus.Experimental experimental}.<br/>
	 *
	 * May be used as the default container for our implementations in the future.
	 *
	 * @deprecated  Experimental
	 */
	@ApiStatus.Experimental
	ComponentContainer.Factory<OnAccessComponentContainer> ON_ACCESS_FACTORY = OnAccessComponentContainer.FACTORY;

	/**
	 * Creates a {@link Factory} to a {@link SingleComponentContainer}.<br/>
	 *
	 * A {@link SingleComponentContainer} is identical to a {@link LazyComponentContainer}, except <b><i>it can only store 1 component</i></b>.
	 *
	 * @param type The {@link ComponentType} to store. The default factory of the type is used for initialization.
	 * @return A new {@link Factory} for {@link SingleComponentContainer} holding the provided {@link ComponentType}.
	 * @param <C> The type of component that is contained by the container.
	 */
	static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentType<C> type) {
		return SingleComponentContainer.createFactory(new ComponentEntry<>(type));
	}

	/**
	 * Identical to {@link ComponentContainer#createSingleFactory(ComponentType)}, except that it allows the caller to specify the {@link Component.Factory} used.
	 * @see ComponentContainer#createSingleFactory(ComponentType)
	 */
	static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentType<C> type, Component.Factory<C> factory) {
		return SingleComponentContainer.createFactory(new ComponentEntry<>(type, factory));
	}

	/**
	 * Creates a {@link CompositeComponentContainer} using the 2 {@linkplain ComponentContainer containers} provided.
	 *
	 * @param main The main container.
	 * @param fallback The secondary container.
	 * @return A {@link CompositeComponentContainer} which basically just wraos the other two containers and functions as their union.
	 */
	static ComponentContainer createComposite(ComponentContainer main, ComponentContainer fallback) {
		return new CompositeComponentContainer(main, fallback);
	}

	/**
	 * Creates an {@link Builder} targetting the provided object as its provider.
	 *
	 * @param obj The object that the {@link Builder} will target.
	 * @return A new {@link Builder} instance.
	 * @throws UnsupportedOperationException If the provided object does not implement {@link ComponentProvider}.
	 */
	static Builder builder(Object obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			throw new UnsupportedOperationException("Cannot create a container for a non-provider object");
		}

		return new Builder(provider);
	}

	/**
	 * The deepest level implementation of {@link org.quiltmc.qsl.component.api.Components#expose}.<br/>
	 * This takes in a wildcarded type and only returns a {@link Component} without casting it.<br/>
	 * This is type-unsafe and if not implemented correctly, may lead to crashes with {@link ClassCastException}s.<br/>
	 *
	 * This method is called <i>really</i> often, so it is advised you make it have a really fast runtime.
	 *
	 * @param type The type to expose.
	 * @return Should return {@link org.quiltmc.qsl.base.api.util.Maybe.Nothing} if the provided {@link ComponentType} is not contained in the current container.
	 *		   Otherwise, should always return {@link org.quiltmc.qsl.base.api.util.Maybe.Just} the contained {@link Component} instance.
	 */
	Maybe<Component> expose(ComponentType<?> type);

	/**
	 * Serializes the {@link org.quiltmc.qsl.component.api.component.NbtComponent}s this {@linkplain ComponentContainer container} contains, into the provided nbt tag.
	 *
	 * @param providerRootNbt The root nbt tag of the provider containing this {@link ComponentContainer}.
	 */
	void writeNbt(NbtCompound providerRootNbt);

	/**
	 * Deserializes the {@link org.quiltmc.qsl.component.api.component.NbtComponent}s this {@link ComponentContainer container} contains, from the provided nbt tag.<br/>
	 *
	 * @implNote There is no guarantee that the provided tag will contain exactly the same components as the container.
	 * It may contain less or more. Keep that in mind when creating custom implementations.
	 *
	 * @param providerRootNbt The root nbt tag of the provider containing this {@link ComponentContainer}.
	 */
	void readNbt(NbtCompound providerRootNbt);

	/**
	 * Ticks all {@link org.quiltmc.qsl.component.api.component.TickingComponent}s contained in this {@link ComponentContainer}.
	 *
	 * @param provider The provider is passed in so that {@link org.quiltmc.qsl.component.api.component.TickingComponent}s can use it.
	 */
	void tick(ComponentProvider provider);

	/**
	 * Syncs all {@link org.quiltmc.qsl.component.api.component.SyncedComponent}s contained in this {@link ComponentContainer}, provided they need syncing.
	 * Most of the time this method is invoked by {@link ComponentContainer#tick}.<br/>
	 *
	 * @param provider The provider is passed in so that {@link SyncChannel#syncFromQueue} can use it, when creating a packet to sync data to the client.
	 * @implNote The best way to sync components at the moment is using {@link SyncChannel#syncFromQueue}, which takes in a {@link Queue} of components that need syncing.
	 */
	void sync(ComponentProvider provider);

	/**
	 * Runs the specified action over all entries of this container.<br/>
	 *
	 * @apiNote May be slow, so use sparingly!
	 */
	void forEach(BiConsumer<ComponentType<?>, ? super Component> action);

	/**
	 * An interface representing the way to create a new {@link ComponentContainer}.
	 *
	 * @param <T> The type of container created by calling {@link Factory#generate}.
	 * @author 0xJoeMama
	 */
	@FunctionalInterface
	interface Factory<T extends ComponentContainer> {
		/**
		 * Creates a {@link ComponentContainer} using the specified arguments.
		 *
		 * @param provider The {@link ComponentProvider} that will contain the created {@link ComponentContainer}.
		 * @param entries A {@link List} containing all the entries that were manually added by the {@link Builder}.
		 * @param saveOperation The operation to be run by the contained {@link Component}s to mark the {@linkplain ComponentProvider provider} as needing to save.
		 * @param ticking Whether this container can tick.
		 * @param syncChannel The {@link SyncChannel} representing the {@link ComponentProvider} that contains the created {@link ComponentContainer}.
		 * @return A {@link ComponentContainer} created using the specified parameters.
		 */
		T generate(ComponentProvider provider,
				   Lazy<List<ComponentEntry<?>>> entries,
				   @Nullable Runnable saveOperation,
				   boolean ticking,
				   @Nullable SyncChannel<?, ?> syncChannel);
	}

	/**
	 * Base {@link Builder} class to create {@link ComponentContainer}s.
	 *
	 * @apiNote You may create custom implementations of this class, if it is needed.
	 */
	class Builder {
		private final ComponentProvider provider;
		private final Lazy<List<ComponentEntry<?>>> injections;
		private boolean ticking;
		@Nullable
		private Runnable saveOperation;
		@Nullable
		private SyncChannel<?, ?> syncChannel;

		private Builder(ComponentProvider provider) {
			this.provider = provider;
			this.injections = Lazy.of(ArrayList::new);
			this.saveOperation = null;
			this.syncChannel = null;
		}

		public Builder saving(Runnable saveOperation) {
			this.saveOperation = saveOperation;
			return this;
		}

		public Builder ticking() {
			this.ticking = true;
			return this;
		}

		public Builder syncing(SyncChannel<?, ?> syncChannel) {
			this.syncChannel = syncChannel;
			return this;
		}

		public <C extends Component> Builder add(ComponentEntry<C> componentEntry) {
			this.injections.get().add(componentEntry);
			return this;
		}

		public <C extends Component> Builder add(ComponentType<C> type) {
			this.add(new ComponentEntry<>(type));
			return this;
		}

		public <C extends Component> Builder add(ComponentType<C> type, Component.Factory<C> factory) {
			this.add(new ComponentEntry<>(type, factory));
			return this;
		}

		public Builder add(ComponentType<?>... types) {
			for (var type : types) {
				this.add(type);
			}

			return this;
		}

		public <T extends ComponentContainer> T build(ComponentContainer.Factory<T> factory) {
			// TODO: See if we can cache the builder at some stage to reduce object creation.
			return factory.generate(this.provider, this.injections, this.saveOperation, this.ticking, this.syncChannel);
		}
	}
}
