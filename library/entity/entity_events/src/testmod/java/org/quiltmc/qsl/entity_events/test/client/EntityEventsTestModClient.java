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

package org.quiltmc.qsl.entity_events.test.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.particle.ParticleTypes;
import org.quiltmc.qsl.entity_events.api.client.ClientEntityLoadEvents;
import org.quiltmc.qsl.entity_events.api.client.ClientEntityTickCallback;
import org.quiltmc.qsl.entity_events.test.EntityEventsTestMod;

public class EntityEventsTestModClient implements ClientEntityLoadEvents.AfterLoad, ClientEntityLoadEvents.AfterUnload, ClientEntityTickCallback {
	// Chicken Loading is logged.
	@Override
	public void onLoadClient(Entity entity, ClientWorld world) {
		if (entity instanceof ChickenEntity) {
			EntityEventsTestMod.LOGGER.info("Chicken loaded, client");
		}
	}

	// Skeleton Unloading is logged.
	@Override
	public void onUnloadClient(Entity entity, ClientWorld world) {
		if (entity instanceof SkeletonEntity) {
			EntityEventsTestMod.LOGGER.info("Skeleton unloaded, client");
		}
	}

	// Slimes will emit explosion particles, unless they're riding something
	@Override
	public void onClientEntityTick(Entity entity, boolean isPassengerTick) {
		if (entity instanceof SlimeEntity && !isPassengerTick) {
			entity.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY(), entity.getZ(), 0.0, 0.0, 0.0);
		}
	}
}
