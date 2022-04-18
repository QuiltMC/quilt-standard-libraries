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

package org.quiltmc.qsl.entity_events.test;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.entity_events.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityEventsTestMod implements EntityReviveEvents.TryReviveAfterTotem,
		EntityReviveEvents.TryReviveBeforeTotem,
		LivingEntityDeathCallback,
		ServerEntityLoadEvents.AfterLoad,
		ServerEntityLoadEvents.AfterUnload,
		EntityWorldChangeEvents.AfterPlayerChange,
		EntityWorldChangeEvents.AfterEntityChange,
		ServerPlayerEntityCopyCallback {
	public static final Logger LOGGER = LoggerFactory.getLogger("quilt_entity_events_testmod");

	// When an entity is holding an allium in its main hand at death and nothing else revives it, it will be
	// revived with 10 health.
	@Override
	public boolean tryReviveAfterTotem(LivingEntity entity, DamageSource damagesource) {
		if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.ALLIUM)) {
			entity.setHealth(10f);
			return true;
		}
		return false;
	}

	// When an entity is holding an azure bluet in its main hand at death, before the totems of undying kick in, it
	// will be revived with 10 health.
	@Override
	public boolean tryReviveBeforeTotem(LivingEntity entity, DamageSource damagesource) {
		if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.AZURE_BLUET)) {
			entity.setHealth(10f);
			return true;
		}
		return false;
	}

	// All invocations of the livingentity death event are logged.
	@Override
	public void onDeath(LivingEntity killed, DamageSource source) {
		LOGGER.info(source.getName() + " killed " + killed.getName().getString());
	}

	// Chicken Loading is logged.
	@Override
	public void onLoad(Entity entity, ServerWorld world) {
		if (entity instanceof ChickenEntity) {
			LOGGER.info("Chicken loaded, server");
		}
	}

	// Skeleton Unloading is logged.
	@Override
	public void onUnload(Entity entity, ServerWorld world) {
		if (entity instanceof SkeletonEntity) {
			LOGGER.info("Skeleton unloaded, server");
		}
	}

	// Players going to another world are notified
	@Override
	public void afterChangeWorld(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) {
		player.sendMessage(new LiteralText("Welcome to "+destination.getRegistryKey().toString()), false);
	}

	// Entities going to the end are named 'end traveller'
	@Override
	public void afterChangeWorld(Entity originalEntity, Entity newEntity, ServerWorld origin, ServerWorld destination) {
		if (destination.getDimension() == destination.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_END_REGISTRY_KEY)) {
			newEntity.setCustomName(new LiteralText("End Traveller"));
		}
	}

	// Players keep the glowing effect after respawn
	// Players receive an apple after coming back from the end
	@Override
	public void onPlayerCopy(ServerPlayerEntity newPlayer, ServerPlayerEntity original, boolean wasDeath) {
		if (wasDeath) {
			var glowingEffect = original.getStatusEffect(StatusEffects.GLOWING);
			if (glowingEffect != null) {
				newPlayer.addStatusEffect(glowingEffect);
			}
		} else {
			newPlayer.giveItemStack(Items.APPLE.getDefaultStack());
		}
	}
}
