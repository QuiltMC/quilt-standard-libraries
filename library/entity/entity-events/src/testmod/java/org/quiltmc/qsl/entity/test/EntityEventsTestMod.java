package org.quiltmc.qsl.entity.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.entity.api.event.*;

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

		// Zombies with custom names fly (common side test) and have a trail of barrier particles (client side test).
		// Entities which are passengers are given acacia signs in their main hand slot.
		EntityTickCallback.ENTITY_TICK.register((entity, isWorldClient, isPassengerTick) -> {
			if (entity instanceof ZombieEntity && entity.hasCustomName()) {
				entity.addVelocity(0, 0.08, 0);
				if (isWorldClient) {
					for (int i = 0; i < 20; i++) {
						entity.world.addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY(), entity.getZ(), 0, -0.1, 0);
					}
				}
			}

			if (isPassengerTick) {
				entity.equipStack(EquipmentSlot.MAINHAND, Items.ACACIA_SIGN.getDefaultStack());
			}
		});

		// All invocations of this event are logged.
		EntityKilledCallback.EVENT.register((world, killer, killed) -> {
			if (killer != null) {
				LOGGER.info(killer.getName().getString() + " killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			} else {
				LOGGER.info("Something killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			}
		});

		// Chickens, on load, create a beacon activate sound (client side test) and are set on fire for 2s (server side test).
		EntityLoadEvents.AFTER_ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ChickenEntity) {
				if (world.isClient) {
					world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL, 1f, 1f, false);
				} else {
					entity.setOnFireFor(2);
				}
			}
		});

		// Skeleton Unloading is logged.
		EntityLoadEvents.AFTER_ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof SkeletonEntity) {
				LOGGER.info("Skeleton unloaded, {}", world.isClient ? "client" : "server");
			}
		});

		// Players going to the nether are notified
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			if (destination.getDimension() == destination.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_NETHER_REGISTRY_KEY)) {
				player.sendMessage(new LiteralText("Nether Entered"), false);
			}
		});

		// Entities going to the end are named 'end traveller'
		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
			if (destination.getDimension() == destination.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_END_REGISTRY_KEY)) {
				newEntity.setCustomName(new LiteralText("End Traveller"));
			}
		});
	}
}
