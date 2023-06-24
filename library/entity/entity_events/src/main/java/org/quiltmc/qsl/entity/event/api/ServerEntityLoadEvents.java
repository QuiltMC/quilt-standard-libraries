/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.entity.event.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events related to an entity being loaded into or unloaded from a server world.
 */
public final class ServerEntityLoadEvents {
	/**
	 * An event which is called after an entity has been loaded into a server world.
	 */
	public static final Event<AfterLoad> AFTER_LOAD = Event.create(AfterLoad.class, callbacks -> (entity, world) -> {
		for (var callback : callbacks) {
			callback.onLoad(entity, world);
		}
	});

	/**
	 * An event which is called after an entity has been unloaded from a server world.
	 */
	public static final Event<AfterUnload> AFTER_UNLOAD = Event.create(AfterUnload.class, callbacks -> (entity, world) -> {
		for (var callback : callbacks) {
			callback.onUnload(entity, world);
		}
	});

	@FunctionalInterface
	public interface AfterLoad extends EventAwareListener {
		/**
		 * Called after an entity has loaded into a server world.
		 *
		 * @param entity the entity which has been loaded
		 * @param world the world the entity has been loaded into
		 */
		void onLoad(Entity entity, ServerWorld world);
	}

	@FunctionalInterface
	public interface AfterUnload extends EventAwareListener {
		/**
		 * Called after an entity has been unloaded from a server world.
		 *
		 * @param entity the entity which has been unloaded
		 * @param world the world the entity has been unloaded from
		 */
		void onUnload(Entity entity, ServerWorld world);
	}

	private ServerEntityLoadEvents() {}
}
