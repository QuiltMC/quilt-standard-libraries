package org.quiltmc.qsl.entity.networking.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.networking.api.custom_spawn_data.QuiltCustomSpawnDataEntity;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.slf4j.Logger;

public class QuiltEntityNetworkingClientInitializer implements ClientModInitializer {
	private static final Logger logger = LogUtils.getLogger();

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientPlayNetworking.registerGlobalReceiver(
			QuiltCustomSpawnDataEntity.EXTENDED_SPAWN_PACKET,
			(client, handler, buf, sender) -> {
				EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(buf);
				buf.retain(); // make sure data is retained and can be read on the client thread
				client.execute(() -> {
					try {
						spawnPacket.apply(handler);
						Entity spawnedEntity = client.world.getEntityById(spawnPacket.getId());
						if (spawnedEntity instanceof QuiltCustomSpawnDataEntity customDataEntity) {
							customDataEntity.readCustomSpawnData(buf);
						} else {
							logger.error(
								"[Quilt] invalid entity received for extended spawn packet; expected a QuiltCustomSpawnDataEntity, got: " +
									(spawnedEntity == null ? "null" : Registries.ENTITY_TYPE.getId(spawnedEntity.getType()).toString())
							);
						}
					} finally { // make sure the buffer is released after
						buf.release();
					}
				});
			}
		);
	}
}
