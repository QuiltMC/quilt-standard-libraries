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

package org.quiltmc.qsl.entity_events.test.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.ChickenEntity;
import org.quiltmc.qsl.entity_events.api.client.ClientEntityLoadEvents;
import org.quiltmc.qsl.entity_events.test.EntityEventsTestMod;

public class EntityEventsTestModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Chicken Loading is logged.
		ClientEntityLoadEvents.AFTER_LOAD.register((entity, world) -> {
			if (entity instanceof ChickenEntity) {
				EntityEventsTestMod.LOGGER.info("Chicken loaded, client");
			}
		});

		// Skeleton Unloading is logged.
		ClientEntityLoadEvents.AFTER_UNLOAD.register((entity, world) -> {
			if (entity instanceof SkeletonEntity) {
				EntityEventsTestMod.LOGGER.info("Skeleton unloaded, client");
			}
		});
	}
}
