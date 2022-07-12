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
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;

import java.util.HashMap;
import java.util.Map;

// Suggestion from Technici4n from fabric. May help improve performance and memory footprint once done.
public class OnAccessComponentContainer extends AbstractComponentContainer {
	private final Map<ComponentType<?>, Component> components;

	private OnAccessComponentContainer(ComponentProvider provider,
									   @Nullable Runnable saveOperation,
									   boolean ticking,
									   @Nullable SyncPacket.SyncContext syncContext) {
		super(saveOperation, ticking, syncContext);
		this.components = this.getInitialComponents(provider);
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return Maybe.wrap(this.components.get(type))
				.or(() -> this.supports(type) ? Maybe.just(this.initializeComponent(this.getInjection(type))) : Maybe.nothing());
	}

	@Override
	protected <COMP extends Component> void addComponent(ComponentType<COMP> type, Component component) {
		this.components.put(type, component);
	}

	private <C extends Component> ComponentEntry<C> getInjection(ComponentType<C> type) {
		throw new UnsupportedOperationException("TODO: NOT IMPLEMENTED");
	}

	private boolean supports(ComponentType<?> type) {
		throw new UnsupportedOperationException("TODO: NOT IMPLEMENTED");
	}

	private Map<ComponentType<?>, Component> getInitialComponents(ComponentProvider provider) {
//		return this.supportedTypes.stream() // TODO: We can cache this value.
//				.filter(((Predicate<ComponentType<?>>) ComponentType::isStatic).or(ComponentType::isInstant))
//				.collect(IdentityHashMap::new, (map, type) -> map.put(type, this.createComponent(type)), Map::putAll);
		return new HashMap<>();
	}
}
