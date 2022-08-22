package org.quiltmc.qsl.entity.custom_spawn_data.impl;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.custom_spawn_data.api.QuiltCustomSpawnDataEntity;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomEntitySpawnDataClientInitializer implements ClientModInitializer {
	private static final Logger logger = LoggerFactory.getLogger("Quilt Custom Entity Spawn Data");

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientPlayNetworking.registerGlobalReceiver(QuiltCustomSpawnDataEntity.EXTENDED_SPAWN_PACKET, (client, handler, buf, sender) -> {
			EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(buf);
			buf.retain(); // save to let the entity read it
			client.execute(() -> {
				try {
					spawnPacket.apply(handler);
					Entity spawnedEntity = client.world.getEntityById(spawnPacket.getId());
					if (!(spawnedEntity instanceof QuiltCustomSpawnDataEntity customDataEntity)) {
						logger.error("[Quilt] invalid entity received for extended spawn packet; expected a QuiltCustomSpawnDataEntity, got: " +
								(spawnedEntity == null ? "null" : Registry.ENTITY_TYPE.getId(spawnedEntity.getType()).toString()));
						return;
					}
					customDataEntity.readCustomSpawnData(buf);
				} finally {
					buf.release();
				}
			});
		});
	}
}
