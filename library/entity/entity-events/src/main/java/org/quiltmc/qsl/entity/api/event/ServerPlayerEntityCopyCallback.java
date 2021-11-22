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

package org.quiltmc.qsl.entity.api.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback which is called on the logical server when a player is copied.
 *
 * <p>Players are copied on death and when returning from the end. The {@code wasDeath} parameter can be used to
 * differentiate between the two situations.
 *
 * <p>The callback is called after vanilla has done its own copying logic.
 *
 * @see ServerPlayerEntity#copyFrom(ServerPlayerEntity, boolean)
 */
@FunctionalInterface
public interface ServerPlayerEntityCopyCallback {
	/**
	 * Invoked when a player is copied on the logical server.
	 */
	ArrayEvent<ServerPlayerEntityCopyCallback> EVENT = ArrayEvent.create(ServerPlayerEntityCopyCallback.class, callbacks -> (newPlayer, original, wasDeath) -> {
		for (var callback : callbacks) {
			callback.onPlayerCopy(newPlayer, original, wasDeath);
		}
	});


	/**
	 * Called when a player is copied.
	 *
	 * @param newPlayer the new ServerPlayerEntity instance
	 * @param original 	the original ServerPlayerEntity instance
	 * @param wasDeath 	true if the copying is due to the player dying, false otherwise
	 */
	void onPlayerCopy(ServerPlayerEntity newPlayer, ServerPlayerEntity original, boolean wasDeath);
}
