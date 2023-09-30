/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.worldgen.dimension.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

@ApiStatus.Internal
public class QuiltDimensionsImpl {
	// Static only-class, no instantiation necessary!
	private QuiltDimensionsImpl() {
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E teleport(Entity entity, ServerWorld destinationWorld, TeleportTarget location) {
		Preconditions.checkArgument(
				Thread.currentThread() == entity.getServer().getThread(),
				"This method may only be called from the main server thread"
		);

		var access = (EntityAccess) entity;
		access.setTeleportTarget(location);

		try {
			// Fast path for teleporting within the same dimension.
			if (entity.getWorld() == destinationWorld) {
				if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
					serverPlayerEntity.networkHandler.requestTeleport(location.position.x, location.position.y, location.position.z, location.yaw, entity.getPitch());
				} else {
					entity.refreshPositionAndAngles(location.position.x, location.position.y, location.position.z, location.yaw, entity.getPitch());
				}

				entity.setVelocity(location.velocity);
				entity.setHeadYaw(location.yaw);

				return (E) entity;
			}

			return (E) entity.moveToWorld(destinationWorld);
		} finally {
			// Always clean up!
			access.setTeleportTarget(null);
		}
	}
}
