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

package org.quiltmc.qsl.entity.interaction.test;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.interaction.api.LivingEntityAttackEvents;
import org.quiltmc.qsl.entity.interaction.api.player.AttackEntityEvents;
import org.quiltmc.qsl.entity.interaction.api.player.BreakBlockEvents;
import org.quiltmc.qsl.entity.interaction.api.player.UseEntityEvents;
import org.quiltmc.qsl.entity.interaction.api.player.UseItemEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionTest implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("qsl_entity_interaction_testmod");

	@Override
	public void onInitialize(ModContainer mod) {
		AttackEntityEvents.BEFORE.register((player, world, stack, entity) -> {
			if (stack.isOf(Items.DIAMOND_SWORD)) {
				return ActionResult.FAIL;
			}
			if (stack.isOf(Items.DIAMOND_SHOVEL)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		LivingEntityAttackEvents.BEFORE.register(context -> {
			if (context.getAttacker() instanceof IronGolemEntity) context.cancel();
		});

		LivingEntityAttackEvents.BEFORE.register(context -> {
			if (context.getAttacker() instanceof ZombieEntity) {
				context.setDamage(context.getDamage() + 3);
				System.out.println(context.getDamage());
			}
		});

		LivingEntityAttackEvents.BEFORE.register(context -> {
			if (context.getAttacker() instanceof ZombieEntity) {
				context.setDamage(context.getDamage() + 2);
				System.out.println(context.getDamage());
			}
		});

		LivingEntityAttackEvents.BEFORE.register(context -> {
			if (context.getAttacker() instanceof ZombieEntity) {
				context.setDamage(context.getDamage() + 1);
				System.out.println(context.getDamage());
			}
		});

		UseEntityEvents.BEFORE.register((player, world, hand, stack, entity, hitResult) -> {
			if (entity instanceof CreeperEntity) {
				if (world instanceof ServerWorld) {
					System.out.println("creeper " + world);
					return ActionResult.FAIL;
				}
			}
			return ActionResult.PASS;
		});

		UseItemEvents.AFTER.register((player, world, hand, stack) -> {
			if (player.getStackInHand(hand).isOf(Items.DIAMOND_SWORD)) {
				LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
				lightning.setPos(player.getX(), player.getY(), player.getZ());
				world.spawnEntity(lightning);
			}
		});

		BreakBlockEvents.BEFORE.register((player, world, stack, pos, state, blockEntity) -> {
			if (state.getBlock() == Blocks.GRASS_BLOCK) {
				//if (world.isClient) return false;
			}
			return true;
		});

		BreakBlockEvents.AFTER.register((player, world, stack, pos, state, blockEntity) -> {
			if (state.getBlock() == Blocks.GRASS_BLOCK) {
				world.setBlockState(pos, Blocks.LAVA.getDefaultState());
			}
		});

		LOGGER.info("Finished Interaction Module test init");
	}
}
