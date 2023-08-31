/*
 * Copyright 2023 The Quilt Project
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
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.registry.Registries;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.networking.api.extended_spawn_data.QuiltExtendedSpawnDataEntity;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

@ApiStatus.Internal
public final class QuiltEntityNetworkingClientInitializer implements ClientModInitializer {
	private static final Logger logger = LogUtils.getLogger();

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientPlayNetworking.registerGlobalReceiver(
				QuiltEntityNetworkingInitializer.EXTENDED_SPAWN_PACKET_ID,
				(client, handler, buf, sender) -> {
					int entityId = buf.readVarInt();
					buf.retain(); // Make sure data is retained and can be read on the client thread
					client.execute(() -> {
						try {
							var entity = client.world.getEntityById(entityId);
							if (entity instanceof QuiltExtendedSpawnDataEntity extended) {
								extended.readAdditionalSpawnData(buf);
							} else {
								var id = entity == null
										? "null"
										: Registries.ENTITY_TYPE.getId(entity.getType()).toString();
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
