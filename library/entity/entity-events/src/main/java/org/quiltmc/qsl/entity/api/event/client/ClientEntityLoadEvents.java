/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.entity.api.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * Events related to an entity being loaded into or unloaded from a client world.
 */
@Environment(EnvType.CLIENT)
public final class ClientEntityLoadEvents {
	/**
	 * An event which is called after an entity has been loaded into a client world.
	 */
	public static final Event<AfterEntityLoadClient> AFTER_ENTITY_LOAD_CLIENT = Event.create(AfterEntityLoadClient.class, callbacks -> (entity, world) -> {
		for (AfterEntityLoadClient callback : callbacks) {
			callback.onLoadClient(entity, world);
		}
	});
	/**
	 * An event which is called after an entity has been unloaded from a client world.
	 */
	public static final Event<AfterEntityUnloadClient> AFTER_ENTITY_UNLOAD_CLIENT = Event.create(AfterEntityUnloadClient.class, callbacks -> (entity, world) -> {
		for (AfterEntityUnloadClient callback : callbacks) {
			callback.onUnloadClient(entity, world);
		}
	});

	@FunctionalInterface
	public interface AfterEntityLoadClient {
		/**
		 * Called after an entity has loaded into a client world.
		 *
		 * @param entity the entity which has been loaded
		 * @param world the world the entity has been loaded into
		 */
		void onLoadClient(Entity entity, ClientWorld world);
	}

	@FunctionalInterface
	public interface AfterEntityUnloadClient {
		/**
		 * Called after an entity has been unloaded from a client world.
		 *
		 * @param entity the entity which has been unloaded
		 * @param world the world the entity has been unloaded from
		 */
		void onUnloadClient(Entity entity, ClientWorld world);
	}

	private ClientEntityLoadEvents() {}
}
