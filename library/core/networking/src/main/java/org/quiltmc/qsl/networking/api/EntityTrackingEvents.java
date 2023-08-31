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

package org.quiltmc.qsl.networking.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events related to a tracking entities within a player's view distance.
 */
public final class EntityTrackingEvents {
	/**
	 * An event that is called before player starts tracking an entity.
	 * Typically, this occurs when an entity enters a client's view distance.
	 * This event is called before the player's client is sent the entity's {@link Entity#createSpawnPacket() spawn packet}.
	 *
	 * @apiNote Since the client will not know about the entity at this point, you probably don't want to send packets
	 * referencing the entity here. Do that in {@link #AFTER_START_TRACKING} instead.
	 */
	public static final Event<BeforeStartTracking> BEFORE_START_TRACKING = Event.create(BeforeStartTracking.class, callbacks -> (trackedEntity, player) -> {
		for (BeforeStartTracking callback : callbacks) {
			callback.beforeStartTracking(trackedEntity, player);
		}
	});

	/**
	 * An event that is called after a player starts tracking an entity.
	 * Typically, this occurs when an entity enters a client's view distance.
	 * This event is called after the player's client is sent the entity's {@link Entity#createSpawnPacket() spawn packet},
	 * so packets may be sent referencing the entity.
	 *
	 * @apiNote If you're using this to tell the client information for <em>your own</em> entity, you may want to instead override {@link Entity#onStartedTrackingBy(ServerPlayerEntity)}.
	 */
	public static final Event<AfterStartTracking> AFTER_START_TRACKING = Event.create(AfterStartTracking.class, callbacks -> (trackedEntity, player) -> {
		for (AfterStartTracking callback : callbacks) {
			callback.afterStartTracking(trackedEntity, player);
		}
	});

	/**
	 * An event that is called after a player has stopped tracking an entity.
	 * The client at this point was sent a packet to {@link net.minecraft.network.packet.s2c.play.EntityDestructionS2CPacket destroy} the entity on the client.
	 * The entity still exists on the server.
	 */
	public static final Event<StopTracking> STOP_TRACKING = Event.create(StopTracking.class, callbacks -> (trackedEntity, player) -> {
		for (StopTracking callback : callbacks) {
			callback.onStopTracking(trackedEntity, player);
		}
	});

	@FunctionalInterface
	public interface BeforeStartTracking extends EventAwareListener {
		/**
		 * Called before an entity starts getting tracked by a player.
		 *
		 * @param trackedEntity the entity that will be tracked
		 * @param player        the player that will track the entity
		 */
		void beforeStartTracking(Entity trackedEntity, ServerPlayerEntity player);
	}

	@FunctionalInterface
	public interface AfterStartTracking extends EventAwareListener {
		/**
		 * Called after an entity starts getting tracked by a player.
		 *
		 * @param trackedEntity the entity that is now being tracked
		 * @param player        the player that is now tracking the entity
		 */
		void afterStartTracking(Entity trackedEntity, ServerPlayerEntity player);
	}

	@FunctionalInterface
	public interface StopTracking extends EventAwareListener {
		/**
		 * Called after an entity stops getting tracked by a player.
		 *
		 * @param trackedEntity the entity that is no longer being tracked
		 * @param player        the player that is no longer tracking the entity
		 */
		void onStopTracking(Entity trackedEntity, ServerPlayerEntity player);
	}

	private EntityTrackingEvents() {}
}
