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

package org.quiltmc.qsl.entity.interaction.api.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

import javax.annotation.Nullable;

@FunctionalInterface
public interface UseEntityCallback {
	Event<UseEntityCallback> EVENT = Event.create(UseEntityCallback.class,
			callbacks -> (player, world, hand, entity, hitResult) -> {
				for (UseEntityCallback callback : callbacks) {
					ActionResult result = callback.onUseEntity(player, world, hand, entity, hitResult);

					if (result != ActionResult.PASS) return result;
				}
				return ActionResult.PASS;
			});

	/**
	 * Invoked when a player uses (right-clicks) an entity.
	 *
	 * @param player the interacting player
	 * @param world the world the event occurs in
	 * @param hand the hand used
	 * @param entity the right-clicked entity
	 * @param hitResult the hit result of the interaction
	 * @return SUCCESS to cancel processing and send a packet to the server, PASS to fall back to further processing,
	 * and FAIL to cancel further processing entirely
	 */
	ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult);
}
