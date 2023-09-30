/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.entity.event.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * A callback which is invoked on the logical server when a LivingEntity dies.
 */
@FunctionalInterface
public interface LivingEntityDeathCallback extends EventAwareListener {
	/**
	 * Invoked when an entity dies server-side.
	 */
	Event<LivingEntityDeathCallback> EVENT = Event.create(LivingEntityDeathCallback.class, callbacks -> (killed, source) -> {
		for (var callback : callbacks) {
			callback.onDeath(killed, source);
		}
	});

	/**
	 * Called when an entity dies.
	 *
	 * @param killed the entity which died
	 * @param source the fatal damage source
	 */
	void onDeath(LivingEntity killed, DamageSource source);
}
