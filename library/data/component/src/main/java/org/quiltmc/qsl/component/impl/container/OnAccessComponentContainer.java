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

import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

// Suggestion from Technici4n from fabric. May help improve performance and memory footprint once done.
public class OnAccessComponentContainer extends AbstractComponentContainer {
	public static final Factory<OnAccessComponentContainer> FACTORY = OnAccessComponentContainer::new;

	private final Map<ComponentType<?>, Either<ComponentEntry<?>, Object>> components;

	protected OnAccessComponentContainer(ComponentProvider provider,
			List<ComponentEntry<?>> entries,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		super(saveOperation, ticking, syncChannel);
		this.components = this.createComponents(entries, provider);
	}

	@Override
	public <C> Maybe<C> expose(ComponentType<C> type) {
		return Maybe.fromOptional(this.components.get(type).right())
					.or(() -> this.supports(type) ?
							  Maybe.just(this.initializeComponent(this.components.get(type).left().orElseThrow())) :
							  Maybe.nothing())
					.castUnchecked();
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		this.components.forEach((type, either) -> either.ifRight(component -> action.accept(type, component)));
	}

	@Override
	protected <COMP> void addComponent(ComponentType<COMP> type, COMP component) {
		this.components.put(type, Either.right(component));
	}

	private Map<ComponentType<?>, Either<ComponentEntry<?>, Object>> createComponents(List<ComponentEntry<?>> entries, ComponentProvider ignoredProvider) {
		var map = new IdentityHashMap<ComponentType<?>, Either<ComponentEntry<?>, Object>>();
		entries.forEach(entry -> {
			ComponentType<?> type = entry.type();
			if (type.isStatic() || type.isInstant()) {
				this.initializeComponent(entry);
			} else {
				map.put(type, Either.left(entry));
			}
		});

		return map;
	}

	private boolean supports(ComponentType<?> type) {
		return this.components.containsKey(type);
	}
}
