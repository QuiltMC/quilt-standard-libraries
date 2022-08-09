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
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

// Suggestion from Technici4n from fabric. May help improve performance and memory footprint once done.
public class OnAccessComponentContainer extends AbstractComponentContainer {
	public static final Factory<OnAccessComponentContainer> FACTORY = OnAccessComponentContainer::new;

	private final Map<ComponentType<?>, ComponentEntry<?>> supported;
	private final Map<ComponentType<?>, Object> components;

	protected OnAccessComponentContainer(ComponentProvider provider,
			List<ComponentEntry<?>> entries,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		super(saveOperation, ticking, syncChannel);
		this.supported = new IdentityHashMap<>();
		this.components = this.createComponents(entries, provider);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <C> C expose(ComponentType<C> type) {
		return (C) this.components.computeIfAbsent(type, innerType -> {
			if (this.supported.containsKey(innerType)) {
				ComponentEntry<?> entry = this.supported.get(innerType);

				if (entry != null) {
					this.supported.remove(innerType);
					return this.initializeComponent(entry);
				}
			}

			return null;
		});
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		this.components.forEach(action);
	}

	@Override
	protected <COMP> void addComponent(ComponentType<COMP> type, COMP component) {
		this.components.put(type, Either.right(component));
	}

	private Map<ComponentType<?>, Object> createComponents(List<ComponentEntry<?>> entries,
			ComponentProvider ignoredProvider) {
		var ret = new IdentityHashMap<ComponentType<?>, Object>();

		for (ComponentEntry<?> entry : entries) {
			ComponentType<?> type = entry.type();

			// If the type can be instantly initialized we initialize it!
			if (type.isInstant() || type.isStatic()) {
				ret.computeIfAbsent(type, innerType -> this.initializeComponent(entry));
				this.supported.remove(type);
				continue;
			}

			// Otherwise we just mark it as supported
			this.supported.putIfAbsent(type, entry);
		}

		return ret;
	}
}
