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

package org.quiltmc.qsl.component.impl.container;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

public class LazyComponentContainer extends AbstractComponentContainer {
	public static final ComponentContainer.Factory<LazyComponentContainer> FACTORY =
			(provider, injections, saveOperation, ticking, syncChannel) ->
					new LazyComponentContainer(provider, saveOperation, ticking, syncChannel);
	private final Map<ComponentType<?>, Lazy<Maybe.Just<Component>>> components;

	protected LazyComponentContainer(
			ComponentProvider provider,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel
	) {
		super(saveOperation, ticking, syncChannel);
		this.components = this.createLazyMap(provider);
	}

	public static void move(LazyComponentContainer from, LazyComponentContainer into) {
		from.components.forEach((id, componentLazy) -> componentLazy.ifFilled(component -> {
			into.components.put(id, componentLazy); // Directly overriding our value.
		}));

		into.nbtComponents.addAll(from.nbtComponents);
		into.pendingSync.ifJust(intoPending -> from.pendingSync.ifJust(intoPending::addAll));
		into.ticking.ifJust(intoTicking -> from.ticking.ifJust(intoTicking::addAll));

		from.components.clear();
		from.nbtComponents.clear();
		from.ticking.ifJust(List::clear);
		from.pendingSync.ifJust(Queue::clear);
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> id) {
		return this.components.containsKey(id) ? this.components.get(id).get() : Maybe.nothing();
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Component> action) {
		// unwrap will work here since all Lazies are Just instances for this.
		this.components.forEach((type, componentLazy) -> componentLazy.ifFilled(component -> action.accept(type, component.unwrap())));
	}

	@Override
	protected <COMP extends Component> void addComponent(ComponentType<COMP> type, Component component) { }

	private Map<ComponentType<?>, Lazy<Maybe.Just<Component>>> createLazyMap(ComponentProvider provider) {
		// TODO: Consider adding a way to directly add components to the builder.
		var map = new IdentityHashMap<ComponentType<?>, Lazy<Maybe.Just<Component>>>();
		ComponentsImpl.getInjections(provider).forEach(injection -> map.put(injection.type(), this.createLazy(injection)));
		return map;
	}

	private <C extends Component> Lazy<Maybe.Just<Component>> createLazy(ComponentEntry<C> componentEntry) {
		ComponentType<?> type = componentEntry.type();

		if (type.isStatic() || type.isInstant()) {
			var component = this.initializeComponent(componentEntry);
			return Lazy.filled((Maybe.Just<Component>) Maybe.just(component)); // this cast will obviously never fail
		}

		return Lazy.of(() -> ((Maybe.Just<Component>) Maybe.just(this.initializeComponent(componentEntry)))); // same here
	}
}
