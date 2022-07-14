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
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;

import java.util.IdentityHashMap;
import java.util.Map;

public record ComponentType<T extends Component>(Identifier id, Component.Factory<T> defaultFactory,
												 boolean isStatic, boolean isInstant) implements Component.Factory<T> {
	public static final NetworkCodec<ComponentType<?>> NETWORK_CODEC =
			NetworkCodec.INT.map(Components.REGISTRY::getRawId, ClientSyncHandler.getInstance()::getType);
	private static final Static STATIC_CACHE = new Static();

	@SuppressWarnings("unchecked")
	public Maybe<T> cast(Component component) {
		try {
			return Maybe.just((T) component);
		} catch (ClassCastException ignored) {
			return Maybe.nothing();
		}
	}

	@Override
	public T create(Runnable saveOperation, Runnable syncOperation) {
		if (this.isStatic) {
			return STATIC_CACHE.getOrCreate(this, saveOperation, syncOperation);
		}
		return this.defaultFactory.create(saveOperation, syncOperation);
	}

	public static class Static {
		private final Map<ComponentType<?>, Component> staticInstances = new IdentityHashMap<>();

		private Static() { }

		@SuppressWarnings("unchecked")
		public <C extends Component> C getOrCreate(ComponentType<C> type, Runnable saveOperation, Runnable syncOperation) {
			if (this.staticInstances.containsKey(type)) {
				return (C) this.staticInstances.get(type);
			} else {
				C singleton = type.defaultFactory.create(saveOperation, syncOperation);
				this.staticInstances.put(type, singleton);
				return singleton;
			}
		}
	}
}
