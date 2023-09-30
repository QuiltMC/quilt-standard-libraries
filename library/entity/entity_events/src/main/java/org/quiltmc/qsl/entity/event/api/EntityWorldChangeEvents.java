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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events related to an entity being moved to another world, run on the server.
 *
 * @apiNote For a {@link ServerPlayerEntity}, please use {@link EntityWorldChangeEvents#AFTER_PLAYER_WORLD_CHANGE}.
 */
public final class EntityWorldChangeEvents {
	/**
	 * An event which is invoked server-side after an entity has been moved to a different world.
	 * <p>
	 * All entities are copied to the destination and the old entity removed.
	 * This event does not apply to the {@link ServerPlayerEntity} since players are physically moved to the new world instead of being copied over.
	 * <p>
	 * A mod may use this event for reference cleanup if it is tracking an entity's current world.
	 *
	 * @see EntityWorldChangeEvents#AFTER_PLAYER_WORLD_CHANGE
	 */
	public static final Event<AfterEntityWorldChange> AFTER_ENTITY_WORLD_CHANGE = Event.create(AfterEntityWorldChange.class, callbacks -> (original, copy, origin, destination) -> {
		for (var callback : callbacks) {
			callback.afterWorldChange(original, copy, origin, destination);
		}
	});

	/**
	 * An event which is invoked server-side after a player has been moved to a different world.
	 * <p>
	 * This is similar to {@link EntityWorldChangeEvents#AFTER_ENTITY_WORLD_CHANGE} but is only called for players.
	 * This is because the player is physically moved to the new world instead of being recreated at the destination.
	 * <p>
	 * Note that returning from the end via an end portal does not count as a world-change in this way.</p>
	 *
	 * @see EntityWorldChangeEvents#AFTER_ENTITY_WORLD_CHANGE
	 * @see ServerPlayerEntityCopyCallback
	 */
	public static final Event<AfterPlayerWorldChange> AFTER_PLAYER_WORLD_CHANGE = Event.create(AfterPlayerWorldChange.class, callbacks -> (player, origin, destination) -> {
		for (var callback : callbacks) {
			callback.afterWorldChange(player, origin, destination);
		}
	});

	@FunctionalInterface
	public interface AfterEntityWorldChange extends EventAwareListener {
		/**
		 * Called after an entity has been recreated at the destination when being moved to a different world.
		 * <p>
		 * Note this event is not called if the entity is a {@link ServerPlayerEntity}.
		 * {@link AfterPlayerWorldChange} should be used to track when a player has changed worlds.
		 *
		 * @param original the original entity
		 * @param copy the new entity at the destination
		 * @param origin the world the original entity is in
		 * @param destination the destination world the new entity is in
		 */
		void afterWorldChange(Entity original, Entity copy, ServerWorld origin, ServerWorld destination);
	}

	@FunctionalInterface
	public interface AfterPlayerWorldChange extends EventAwareListener {
		/**
		 * Called after a player has been moved to different world.
		 *
		 * @param player the player
		 * @param origin the original world the player was in
		 * @param destination the new world the player was moved to
		 */
		void afterWorldChange(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination);
	}

	private EntityWorldChangeEvents() {}
}
