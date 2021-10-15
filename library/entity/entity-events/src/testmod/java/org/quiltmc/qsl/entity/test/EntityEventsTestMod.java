package org.quiltmc.qsl.entity.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.entity.api.event.EntityKilledCallback;
import org.quiltmc.qsl.entity.api.event.EntityTickCallback;
import org.quiltmc.qsl.entity.api.event.TryReviveCallback;

public class EntityEventsTestMod implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		TryReviveCallback.AFTER_TOTEM.register((entity, damagesource) -> {
			if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.ALLIUM)) {
				entity.setHealth(10f);
				return true;
			}
			return false;
		});
		TryReviveCallback.BEFORE_TOTEM.register((entity, damagesource) -> {
			if (entity.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.AZURE_BLUET)) {
				entity.setHealth(10f);
				return true;
			}
			return false;
		});
		EntityTickCallback.ENTITY_TICK.register((entity, isWorldClient, isPassengerTick) -> {
			if (entity instanceof ZombieEntity && entity.hasCustomName()) {
				entity.addVelocity(0, 0.08, 0);
				if (isWorldClient) {
					for (int i = 0; i < 20; i++) {
						entity.world.addParticle(ParticleTypes.BARRIER, entity.getX(), entity.getY(), entity.getZ(), 0, -0.1, 0);
					}
				}
			}

			if (isPassengerTick) {
				entity.equipStack(EquipmentSlot.MAINHAND, Items.ACACIA_SIGN.getDefaultStack());
			}
		});

		EntityKilledCallback.EVENT.register((world, killer, killed) -> {
			if (killer != null) {
				LOGGER.info(killer.getName().getString() + " killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			} else {
				LOGGER.info("Something killed " + killed.getName().getString() + " (" + (world.isClient ? "client" : "server") + ")");
			}
		});
	}
}
