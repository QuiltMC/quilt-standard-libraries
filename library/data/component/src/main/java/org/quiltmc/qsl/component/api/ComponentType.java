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
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public record ComponentType<T extends Component>(Identifier id, Component.Factory<T> factory,
												 boolean isStatic, boolean isInstant) implements Component.Factory<T> {
	public static final Static STATIC_CACHE = new Static();

	@SuppressWarnings("unchecked")
	public Optional<T> cast(Component component) {
		try {
			return Optional.of((T) component);
		} catch (ClassCastException ignored) {
			return Optional.empty();
		}
	}

	@Override
	public @NotNull T create() {
		if (this.isStatic) {
			return STATIC_CACHE.getOrCreate(this);
		}
		return this.factory.create();
	}

	public static class Static {
		private final Map<ComponentType<?>, Component> staticInstances = new IdentityHashMap<>();

		private Static() {

		}

		@SuppressWarnings("unchecked")
		@NotNull <C extends Component> C getOrCreate(ComponentType<C> type) {
			if (this.staticInstances.containsKey(type)) {
				return (C) this.staticInstances.get(type);
			} else {
				C singleton = type.factory.create();
				this.staticInstances.put(type, singleton);
				return singleton;
			}
		}
	}
}
