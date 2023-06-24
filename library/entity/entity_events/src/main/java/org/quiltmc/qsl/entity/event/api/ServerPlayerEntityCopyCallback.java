/*
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

import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * A callback which is called on the logical server when a player is copied.
 * <p>
 * Players are copied on death and when returning from the end through a portal. The {@code wasDeath} parameter can
 * be used to differentiate between the two situations.
 * <p>
 * The callback is called after vanilla has done its own copying logic.
 *
 * @see ServerPlayerEntity#copyFrom(ServerPlayerEntity, boolean)
 * @see EntityWorldChangeEvents#AFTER_PLAYER_WORLD_CHANGE
 */
@FunctionalInterface
public interface ServerPlayerEntityCopyCallback extends EventAwareListener {
	/**
	 * Invoked when a player is copied on the logical server.
	 */
	Event<ServerPlayerEntityCopyCallback> EVENT = Event.create(ServerPlayerEntityCopyCallback.class, callbacks -> (copy, original, wasDeath) -> {
		for (var callback : callbacks) {
			callback.onPlayerCopy(copy, original, wasDeath);
		}
	});

	/**
	 * Called when a player is copied.
	 *
	 * @param copy the new ServerPlayerEntity instance
	 * @param original 	the original ServerPlayerEntity instance
	 * @param wasDeath 	true if the copying is due to the player dying, false otherwise
	 */
	void onPlayerCopy(ServerPlayerEntity copy, ServerPlayerEntity original, boolean wasDeath);
}
