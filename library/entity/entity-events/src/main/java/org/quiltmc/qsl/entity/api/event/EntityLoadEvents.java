/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
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

package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * Events related to an entity being loaded into or unloaded from a world.
 */
public final class EntityLoadEvents {
	/**
	 * An event which is called after an entity has been loaded into a server world.
	 */
	public static final ArrayEvent<AfterEntityLoadServer> AFTER_ENTITY_LOAD_SERVER = ArrayEvent.create(AfterEntityLoadServer.class, callbacks -> (entity, world) -> {
		for (AfterEntityLoadServer callback : callbacks) {
			callback.onLoad(entity, world);
		}
	});

	/**
	 * An event which is called after an entity has been loaded into a client world.
	 */
	public static final ArrayEvent<AfterEntityLoadClient> AFTER_ENTITY_LOAD_CLIENT = ArrayEvent.create(AfterEntityLoadClient.class, callbacks -> (entity, world) -> {
		for (AfterEntityLoadClient callback : callbacks) {
			callback.onLoad(entity, world);
		}
	});

	/**
	 * An event which is called after an entity has been unloaded from a server world.
	 */
	public static final ArrayEvent<AfterEntityUnloadServer> AFTER_ENTITY_UNLOAD_SERVER = ArrayEvent.create(AfterEntityUnloadServer.class, callbacks -> (entity, world) -> {
		for (AfterEntityUnloadServer callback : callbacks) {
			callback.onUnload(entity, world);
		}
	});


	/**
	 * An event which is called after an entity has been unloaded from a client world.
	 */
	public static final ArrayEvent<AfterEntityUnloadClient> AFTER_ENTITY_UNLOAD_CLIENT = ArrayEvent.create(AfterEntityUnloadClient.class, callbacks -> (entity, world) -> {
		for (AfterEntityUnloadClient callback : callbacks) {
			callback.onUnload(entity, world);
		}
	});

	@FunctionalInterface
	public interface AfterEntityLoadServer {
		/**
		 * Called after an entity has loaded into a server world.
		 *
		 * @param entity the entity which has been loaded
		 * @param world the world the entity has been loaded into
		 */
		void onLoad(Entity entity, World world);
	}

	@FunctionalInterface
	public interface AfterEntityUnloadServer {
		/**
		 * Called after an entity has been unloaded from a server world.
		 *
		 * @param entity the entity which has been unloaded
		 * @param world the world the entity has been unloaded from
		 */
		void onUnload(Entity entity, World world);
	}


	@FunctionalInterface
	public interface AfterEntityLoadClient {
		/**
		 * Called after an entity has loaded into a client world.
		 *
		 * @param entity the entity which has been loaded
		 * @param world the world the entity has been loaded into
		 */
		void onLoad(Entity entity, World world);
	}

	@FunctionalInterface
	public interface AfterEntityUnloadClient {
		/**
		 * Called after an entity has been unloaded from a client world.
		 *
		 * @param entity the entity which has been unloaded
		 * @param world the world the entity has been unloaded from
		 */
		void onUnload(Entity entity, World world);
	}

	private EntityLoadEvents() {}
}
