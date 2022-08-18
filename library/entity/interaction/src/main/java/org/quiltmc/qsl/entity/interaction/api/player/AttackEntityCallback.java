/*
 * Copyright 2021-2022 QuiltMC
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
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * A callback that is invoked when a Player attacks (left clicks) an entity.
 */
@FunctionalInterface
public interface AttackEntityCallback {

	/**
	 * Invoked when a Player attacks (left clicks) an entity.
	 *
	 * <p>Upon return:
	 * <ul><li>SUCCESS cancels further processing and, on the client, sends a packet to the server.
	 * <li>PASS falls back to further processing.
	 * <li>FAIL cancels further processing and does not send a packet to the server.</ul>
	 */
	Event<AttackEntityCallback> EVENT = Event.create(AttackEntityCallback.class,
			callbacks -> (player, world, hand, entity) -> {
		for (AttackEntityCallback callback : callbacks) {
			ActionResult result = callback.onAttack(player, world, hand, entity);

			if (result != ActionResult.PASS) return result;
		}
		return ActionResult.PASS;
	});

	ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity);
}
