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

package org.quiltmc.qsl.networking.test.trackingevents;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.quiltmc.qsl.networking.api.EntityTrackingEvents;

public final class NetworkingEntityTrackingEventsTest implements EntityTrackingEvents.AfterStartTracking, EntityTrackingEvents.StartTracking, EntityTrackingEvents.StopTracking {
	// Sends a message to the player when they're about to start tracking an entity.
	@Override
	public void onStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
		player.sendMessage(Text.literal("About to be tracking ").append(trackedEntity.getDisplayName()), false);
	}

	// Sends a message to the player when they're newly tracking an entity.
	@Override
	public void afterStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
		player.sendMessage(Text.literal("Tracking ").append(trackedEntity.getDisplayName()), false);
	}

	// Sends a message to the player when they're no longer tracking an entity.
	@Override
	public void onStopTracking(Entity trackedEntity, ServerPlayerEntity player) {
		player.sendMessage(Text.literal("No longer tracking ").append(trackedEntity.getDisplayName()), false);
	}
}
