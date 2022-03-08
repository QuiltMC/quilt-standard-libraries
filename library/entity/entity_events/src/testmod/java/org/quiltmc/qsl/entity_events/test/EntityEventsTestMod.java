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

package org.quiltmc.qsl.entity_events.test;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity_events.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class EntityEventsTestMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("quilt_entity_events_testmod");

	@Override
	public void onInitialize(ModContainer mod) {
		// When an entity is holding an allium in its main hand at death and nothing else revives it, it will be revived with 10 health.
		EntityReviveEvents.AFTER_TOTEM.register((entity, damagesource) -> {
			if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.ALLIUM)) {
				entity.setHealth(10f);
				return true;
			}
			return false;
		});

		// When an entity is holding an azure bluet in its main hand at death, before the totems of undying kick in, it
		// will be revived with 10 health.
		EntityReviveEvents.BEFORE_TOTEM.register((entity, damagesource) -> {
			if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.AZURE_BLUET)) {
				entity.setHealth(10f);
				return true;
			}
			return false;
		});

		// All invocations of the entity killed event are logged.
		EntityKilledCallback.EVENT.register((world, killer, killed) -> {
			if (killer != null) {
				LOGGER.info(killer.getName().getString() + " killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			} else {
				LOGGER.info("Something killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			}
		});

		// Chicken Loading is logged.
		ServerEntityLoadEvents.AFTER_LOAD.register((entity, world) -> {
			if (entity instanceof ChickenEntity) {
				LOGGER.info("Chicken loaded, server");
			}
		});

		// Skeleton Unloading is logged.
		ServerEntityLoadEvents.AFTER_UNLOAD.register((entity, world) -> {
			if (entity instanceof SkeletonEntity) {
				LOGGER.info("Skeleton unloaded, server");
			}
		});

		// Players going to the nether are notified
		EntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			if (destination.getDimension() == destination.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_NETHER_REGISTRY_KEY)) {
				player.sendMessage(new LiteralText("Nether Entered"), false);
			}
		});

		// Entities going to the end are named 'end traveller'
		EntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
			if (destination.getDimension() == destination.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_END_REGISTRY_KEY)) {
				newEntity.setCustomName(new LiteralText("End Traveller"));
			}
		});

		// Players keep the glowing enchantment after respawn
		// Players receive an apple after coming back from the end
		ServerPlayerEntityCopyCallback.EVENT.register((newPlayer, original, wasDeath) -> {
			if (wasDeath) {
				var glowingEffect = original.getStatusEffect(StatusEffects.GLOWING);
				if (glowingEffect != null) {
					newPlayer.addStatusEffect(glowingEffect);
				}
			} else {
				newPlayer.giveItemStack(Items.APPLE.getDefaultStack());
			}
		});
	}
}