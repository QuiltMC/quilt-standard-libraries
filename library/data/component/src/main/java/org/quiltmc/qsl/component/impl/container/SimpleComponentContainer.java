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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public class SimpleComponentContainer extends AbstractComponentContainer {
	public static final ComponentContainer.Factory<SimpleComponentContainer> FACTORY =
			(provider, entries, saveOperation, ticking, syncChannel) ->
					new SimpleComponentContainer(saveOperation, ticking, syncChannel, entries.stream());

	private final Map<ComponentType<?>, Object> components;

	protected SimpleComponentContainer(@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncContext,
			Stream<ComponentEntry<?>> types) {
		super(saveOperation, ticking, syncContext);
		this.components = new IdentityHashMap<>();
		types.forEach(this::initializeComponent);
		types.close();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	@Override
	public <C> C expose(ComponentType<C> type) {
		return (C) this.components.get(type);
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		this.components.forEach(action);
	}

	@Override
	protected <COMP> void addComponent(ComponentType<COMP> type, COMP component) {
		Object result = this.components.put(type, component);
		if (result != null) {
			throw ErrorUtil.illegalState("Attempted to override a component on a simple container!").get();
		}
	}
}
