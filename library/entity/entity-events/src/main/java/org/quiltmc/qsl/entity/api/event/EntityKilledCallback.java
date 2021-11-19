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

package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback which is invoked on the logical server when an entity is killed by another entity.
 *
 * <p>The killed entity's {@link LivingEntity#getRecentDamageSource()} method can be used to retrieve the damage source
 * which may have killed it.
 */
@FunctionalInterface
public interface EntityKilledCallback {
	/**
	 * Invoked when an entity is killed by another entity.
	 */
	ArrayEvent<EntityKilledCallback> EVENT = ArrayEvent.create(EntityKilledCallback.class, callbacks -> (world, killer, killed) -> {
		for (EntityKilledCallback callback : callbacks) {
			callback.onKilled(world, killer, killed);
		}
	});


	/**
	 * Called when an entity is killed by another entity.
	 *
	 * @param world the world
	 * @param killer the killing entity, possibly null
	 * @param killed the entity which was killed by the {@code killer}
	 */
	void onKilled(World world, @Nullable Entity killer, LivingEntity killed);
}
