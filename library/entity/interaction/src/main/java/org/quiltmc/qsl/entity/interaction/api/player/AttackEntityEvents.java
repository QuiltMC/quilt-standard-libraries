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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

public class AttackEntityEvents {

	/**
	 * A callback that is invoked <strong>before</strong> a {@link PlayerEntity} attacks (left-clicks) an {@link Entity}.
	 * <p>
	 * This is invoked prior to the Spectator check, so make sure you check the game mode!
	 * Implementations should not assume the action has been completed.
	 * <p>
	 * Upon return:
	 * <ul>
	 *     <li>{@link ActionResult#SUCCESS} cancels further processing and, on the client, sends a packet to the server.</li>
	 * 	   <li>{@link ActionResult#PASS} falls back to further processing.</li>
	 *     <li>{@link ActionResult#FAIL} cancels further processing and does not send a packet to the server.</li>
	 * </ul>
	 */
	public static final Event<Before> BEFORE = Event.create(Before.class,
			callbacks -> (player, world, stack, entity) -> {
		for (var callback : callbacks) {
			ActionResult result = callback.beforeAttackEntity(player, world, stack, entity);

			if (result != ActionResult.PASS) return result;
		}
		return ActionResult.PASS;
	});

	/**
	 * A callback that is invoked <strong>after</strong> a {@link PlayerEntity} attacks (left-clicks) an {@link Entity}.
	 */
	public static final Event<After> AFTER = Event.create(After.class,
			callbacks -> (player, world, stack, entity) -> {
				for (var callback : callbacks) {
					callback.afterAttackEntity(player, world, stack, entity);
				}
			});

	public interface Before {
		/**
		 * Invoked <strong>before</strong> a {@link PlayerEntity} attacks (left-clicks) an {@link Entity}.
		 *
		 * @param player the interacting {@link PlayerEntity}
		 * @param world the {@link World} the event occurs in
		 * @param entity the hit {@link Entity}
		 * @return {@link ActionResult#SUCCESS} to cancel processing and send packet to the server,
		 *     {@link ActionResult#PASS} to fall back to further processing,
		 *     {@link ActionResult#FAIL} to cancel further processing
		 */
		ActionResult beforeAttackEntity(PlayerEntity player, World world, ItemStack stack, Entity entity);
	}

	public interface After {
		/**
		 * Invoked <strong>after</strong> a {@link PlayerEntity} attacks (left-clicks) an {@link Entity}.
		 *
		 * @param player the interacting {@link PlayerEntity}
		 * @param world the {@link World} the event occurs in
		 * @param entity the hit {@link Entity}
		 */
		void afterAttackEntity(PlayerEntity player, World world, ItemStack stack, Entity entity);
	}
}
