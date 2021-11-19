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

package org.quiltmc.qsl.entity.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.entity.api.event.EntityKilledCallback;
import org.quiltmc.qsl.entity.api.event.EntityLoadEvents;
import org.quiltmc.qsl.entity.api.event.EntityReviveEvents;
import org.quiltmc.qsl.entity.api.event.EntityWorldChangeEvents;

public class EntityEventsTestMod implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
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

		// All invocations of this event are logged.
		EntityKilledCallback.EVENT.register((world, killer, killed) -> {
			if (killer != null) {
				LOGGER.info(killer.getName().getString() + " killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			} else {
				LOGGER.info("Something killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			}
		});

		// Chicken Loading is logged.
		EntityLoadEvents.AFTER_ENTITY_LOAD_CLIENT.register((entity, world) -> {
			if (entity instanceof ChickenEntity) {
				LOGGER.info("Chicken loaded, client");
				}
		});

		// Chicken Loading is logged.
		EntityLoadEvents.AFTER_ENTITY_LOAD_SERVER.register((entity, world) -> {
			if (entity instanceof ChickenEntity) {
				LOGGER.info("Chicken loaded, server");
			}
		});

		// Skeleton Unloading is logged.
		EntityLoadEvents.AFTER_ENTITY_UNLOAD_SERVER.register((entity, world) -> {
			if (entity instanceof SkeletonEntity) {
				LOGGER.info("Skeleton unloaded, server");
			}
		});

		// Skeleton Unloading is logged.
		EntityLoadEvents.AFTER_ENTITY_UNLOAD_CLIENT.register((entity, world) -> {
			if (entity instanceof SkeletonEntity) {
				LOGGER.info("Skeleton unloaded, client");
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
	}
}
