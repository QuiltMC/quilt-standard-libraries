/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.entity.interaction.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * Invoked when an entity damages another entity.
 *
 * <p>Returning FAIL will cancel further processing and the entity will not take damage.</p>
 */
@FunctionalInterface
public interface LivingEntityAttackCallback {

	/**
	 * Invoked when an entity damages another.
	 */
	Event<LivingEntityAttackCallback> EVENT = Event.create(LivingEntityAttackCallback.class,
			callbacks -> (attacker, target, source, amount) -> {
				for (LivingEntityAttackCallback callback : callbacks) {
					ActionResult result = callback.onAttack(attacker, target, source, amount);

					if (result == ActionResult.FAIL) return result;
				}
				return ActionResult.PASS;
			});

	ActionResult onAttack(LivingEntity attacker, Entity target, DamageSource source, float amount);
}
