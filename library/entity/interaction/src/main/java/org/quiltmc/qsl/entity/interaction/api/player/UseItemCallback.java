/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * Invoked when a player uses (right-clicks) an item.
 * <p>
 * Upon return:
 * <ul>
 *     <li>{@link ActionResult#SUCCESS}/{@link ActionResult#CONSUME}/{@link ActionResult#CONSUME_PARTIAL}
 *         cancels further processing and, on the client, sends a packet to the server.</li>
 *     <li>{@link ActionResult#PASS} falls back to further processing.</li>
 *     <li>{@link ActionResult#FAIL} cancels further processing and does not send a packet to the server.</li>
 * </ul>
 */
@FunctionalInterface
public interface UseItemCallback {

	Event<UseItemCallback> EVENT = Event.create(UseItemCallback.class,
			callbacks -> (player, world, hand, stack) -> {
				for (UseItemCallback callback : callbacks) {
					ActionResult result = callback.onUseItem(player, world, hand, stack);

					if (result != ActionResult.PASS) return result;
				}
				return ActionResult.PASS;
			});

	/**
	 * Invoked when a player uses (right-clicks) with an item.
	 *
	 * @param player the interacting player
	 * @param world the world the event occurs in
	 * @param hand the hand used
	 * @return {@link ActionResult#SUCCESS}/{@link ActionResult#CONSUME}/{@link ActionResult#CONSUME_PARTIAL}
	 *     to cancel processing and send a packet to the server,
	 *     {@link ActionResult#PASS} to fall back to further processing,
	 *     {@link ActionResult#FAIL} to cancel further processing.
	 */
	ActionResult onUseItem(PlayerEntity player, World world, Hand hand, ItemStack stack);
}
