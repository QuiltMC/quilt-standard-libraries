/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.event.api.client;

import net.minecraft.entity.Entity;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * A callback that is invoked when an Entity is ticked on the logical client (nominally every 1/20 of a second).
 * <p>
 * There are two types of entity tick - standalone ({@link Entity#tick()}) and riding ({@link Entity#tickRiding()}).
 * This callback takes a parameter which specifies which type of tick it is.
 */
@ClientOnly
@FunctionalInterface
public interface ClientEntityTickCallback extends ClientEventAwareListener {
	/**
	 * Called when an entity is ticked.
	 */
	Event<ClientEntityTickCallback> EVENT = Event.create(ClientEntityTickCallback.class, callbacks -> (entity, isPassengerTick) -> {
		for (ClientEntityTickCallback callback : callbacks) {
			callback.onClientEntityTick(entity, isPassengerTick);
		}
	});

	/**
	 * Called when an entity is ticked on the logical client.
	 *
	 * @param entity the entity
	 * @param isPassengerTick whether the entity is being ticked as a passenger of another entity
	 */
	void onClientEntityTick(Entity entity, boolean isPassengerTick);
}
