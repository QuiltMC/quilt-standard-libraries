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
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.*;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.SyncChannel;

import java.util.ArrayList;
import java.util.List;

public interface ComponentContainer {
	ComponentContainer EMPTY = EmptyComponentContainer.INSTANCE;
	ComponentContainer.Factory<EmptyComponentContainer> EMPTY_FACTORY = EmptyComponentContainer.FACTORY;
	ComponentContainer.Factory<SimpleComponentContainer> SIMPLE_FACTORY = SimpleComponentContainer.FACTORY;
	ComponentContainer.Factory<LazyComponentContainer> LAZY_FACTORY = LazyComponentContainer.FACTORY;
	ComponentContainer.Factory<OnAccessComponentContainer> ON_ACCESS_FACTORY = OnAccessComponentContainer.FACTORY;

	static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentType<C> type) {
		return SingleComponentContainer.createFactory(new ComponentEntry<>(type));
	}

	static <C extends Component> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentEntry<C> entry) {
		return SingleComponentContainer.createFactory(entry);
	}

	static Builder builder(Object obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			throw new UnsupportedOperationException("Cannot create a container for a non-provider object");
		}

		return new Builder(provider);
	}

	Maybe<Component> expose(ComponentType<?> type);

	void writeNbt(NbtCompound providerRootNbt);

	void readNbt(NbtCompound providerRootNbt);

	void tick(ComponentProvider provider);

	void sync(ComponentProvider provider);

	@FunctionalInterface
	interface Factory<T extends ComponentContainer> {
		T generate(ComponentProvider provider,
				   Lazy<List<ComponentEntry<?>>> injections,
				   @Nullable Runnable saveOperation,
				   boolean ticking,
				   @Nullable SyncChannel<?> syncChannel);
	}

	class Builder {
		private final ComponentProvider provider;
		private final Lazy<List<ComponentEntry<?>>> injections;
		private boolean ticking;
		@Nullable
		private Runnable saveOperation;
		@Nullable
		private SyncChannel<?> syncChannel;

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

		public Builder syncing(SyncChannel<?> syncChannel) {
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
			return factory.generate(this.provider, this.injections, this.saveOperation, this.ticking, this.syncChannel);
		}
	}
}
