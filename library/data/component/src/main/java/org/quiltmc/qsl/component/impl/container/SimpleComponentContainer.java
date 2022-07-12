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
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SimpleComponentContainer extends AbstractComponentContainer {
	public static final ComponentContainer.Factory<SimpleComponentContainer> FACTORY =
			(provider, injections, saveOperation, ticking, syncContext) -> new SimpleComponentContainer(
					saveOperation, ticking, syncContext, injections.get().stream()
			);
	private final Map<ComponentType<?>, Component> components;

	protected SimpleComponentContainer(Runnable saveOperation,
									   boolean ticking,
									   @Nullable SyncPacket.SyncContext syncContext,
									   Stream<ComponentEntry<?>> types) {
		super(saveOperation, ticking, syncContext);
		this.components = new IdentityHashMap<>();
		types.forEach(this::initializeComponent);
		types.close();
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return Maybe.wrap(this.components.get(type));
	}

	@Override
	protected <COMP extends Component> void addComponent(ComponentType<COMP> type, Component component) {
		Component result = this.components.put(type, component);
		if (result != null) {
			throw ErrorUtil.illegalState("Attempted to override a component on a simple container!").get();
		}
	}
}
