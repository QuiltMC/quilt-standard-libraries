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
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.Lazy;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;

public class LazyComponentContainer extends AbstractComponentContainer {
	public static final ComponentContainer.Factory<LazyComponentContainer> FACTORY =
			(provider, injections, saveOperation, ticking, syncContext) ->
					new LazyComponentContainer(provider, saveOperation, ticking, syncContext);
	private final IdentityHashMap<ComponentType<?>, Lazy<Component>> components;

	protected LazyComponentContainer(
			ComponentProvider provider,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncPacket.SyncContext syncContext
	) {
		super(saveOperation, ticking, syncContext);
		this.components = this.initializeComponents(provider);
	}

	public static void move(LazyComponentContainer from, LazyComponentContainer into) {
		from.components.forEach((id, componentLazy) -> componentLazy.ifPresent(component -> {
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
		return Maybe.wrap(this.components.get(id)).map(Lazy::get);
	}

	@Override
	protected <COMP extends Component> void addComponent(ComponentType<COMP> type, Component component) {

	}

	private IdentityHashMap<ComponentType<?>, Lazy<Component>> initializeComponents(ComponentProvider provider) {
		var map = new IdentityHashMap<ComponentType<?>, Lazy<Component>>();
		ComponentsImpl.getInjections(provider).forEach(injection -> map.put(injection.type(), this.createLazy(injection)));
		return map;
	}

	private <C extends Component> Lazy<Component> createLazy(ComponentEntry<C> componentEntry) {
		ComponentType<?> type = componentEntry.type();

		if (type.isStatic() || type.isInstant()) {
			var component = this.initializeComponent(componentEntry);

			return Lazy.filled(component);
		}

		return Lazy.of(() -> this.initializeComponent(componentEntry));
	}
}
