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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * Contains events related to a Player right-clicking a block.
 */
public class UseBlockEvents {

	/**
	 * A callback that is invoked <strong>before</strong> a {@link PlayerEntity} right-clicks a block.
	 * <p>
	 * Implementations should not assume the action has been completed.
	 * <p>
	 * Upon return:
	 * <ul>
	 *     <li>{@link ActionResult#SUCCESS}/{@link ActionResult#CONSUME}/{@link ActionResult#CONSUME_PARTIAL}
	 *         cancels further processing and, on the client, sends a packet to the server.</li>
	 *     <li>{@link ActionResult#PASS} falls back to further processing.</li>
	 *     <li>{@link ActionResult#FAIL} cancels further processing and does not send a packet to the server.</li>
	 * </ul>
	 */
	public static final Event<Before> BEFORE = Event.create(Before.class,
			callbacks -> (player, world, hand, stack, pos, hitResult) -> {
				for (var callback : callbacks) {
					ActionResult result = callback.beforeUseBlock(player, world, hand, stack, pos, hitResult);

					if (result != ActionResult.PASS) return result;
				}
				return ActionResult.PASS;
			});

	/**
	 * Invoked <strong>after</strong> a {@link PlayerEntity} right-clicks a block.
	 */
	public static final Event<After> AFTER = Event.create(After.class,
			callbacks -> (player, world, hand, stack, pos, hitResult) -> {
				for (var callback : callbacks) {
					callback.afterUseBlock(player, world, hand, stack, pos, hitResult);
				}
			});

	@FunctionalInterface
	public interface Before {
		/**
		 * Invoked <strong>before</strong> a {@link PlayerEntity} right-clicks a block.
		 *
		 * @param player the interacting {@link PlayerEntity}
		 * @param world the {@link World} the event occurs in
		 * @param hand the {@link Hand} used
		 * @param hitResult the {@link BlockHitResult} of the interaction
		 * @return {@link ActionResult#SUCCESS}/{@link ActionResult#CONSUME}/{@link ActionResult#CONSUME_PARTIAL}
		 *     to cancel processing and send a packet to the server,
		 *     {@link ActionResult#PASS} to fall back to further processing,
		 *     {@link ActionResult#FAIL} to cancel further processing.
		 */
		ActionResult beforeUseBlock(PlayerEntity player, World world, Hand hand, ItemStack stack, BlockPos pos, BlockHitResult hitResult);
	}

	@FunctionalInterface
	public interface After {
		/**
		 * Invoked <strong>after</strong> a {@link PlayerEntity} right-clicks a block.
		 *
		 * @param player the interacting {@link PlayerEntity}
		 * @param world the {@link World} the event occurs in
		 * @param hand the {@link Hand} used
		 * @param hitResult the {@link BlockHitResult} of the interaction
		 */
		void afterUseBlock(PlayerEntity player, World world, Hand hand, ItemStack stack, BlockPos pos, BlockHitResult hitResult);
	}
}
