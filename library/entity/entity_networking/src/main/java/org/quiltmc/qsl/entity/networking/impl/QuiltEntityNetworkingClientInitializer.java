/*
 * Copyright 2023 QuiltMC
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
							String id = spawnedEntity == null
								? "null"
								: Registries.ENTITY_TYPE.getId(spawnedEntity.getType()).toString();
							logger.error(
								"[Quilt] invalid entity received for extended spawn packet: entity [" +
									id + "] does not implement QuiltCustomSpawnDataEntity!"
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
