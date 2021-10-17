/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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
	 * An event which is called after an entity has been loaded into a world.
	 */
	public static final ArrayEvent<AfterEntityLoad> AFTER_ENTITY_LOAD = ArrayEvent.create(AfterEntityLoad.class, callbacks -> (entity, world) -> {
		for (AfterEntityLoad callback : callbacks) {
			callback.onLoad(entity, world);
		}
	});

	/**
	 * An event which is called after a player has been unloaded from a world.
	 */
	public static final ArrayEvent<AfterEntityUnload> AFTER_ENTITY_UNLOAD = ArrayEvent.create(AfterEntityUnload.class, callbacks -> (entity, world) -> {
		for (AfterEntityUnload callback : callbacks) {
			callback.onUnload(entity, world);
		}
	});

	@FunctionalInterface
	public interface AfterEntityLoad {
		/**
		 * Called after an entity has loaded into a world.
		 *
		 * @param entity the entity which has been loaded
		 * @param world the world the entity has been loaded into
		 */
		void onLoad(Entity entity, World world);
	}

	@FunctionalInterface
	public interface AfterEntityUnload {
		/**
		 * Called after an entity has been unloaded from a world.
		 *
		 * @param entity the entity which has been unloaded
		 * @param world the world the entity has been unloaded from
		 */
		void onUnload(Entity entity, World world);
	}

	private EntityLoadEvents() {}
}
