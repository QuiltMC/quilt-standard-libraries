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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.entity.interaction.impl.DamageContext;

/**
 * Contains events that invoke when a {@link LivingEntity} attacks another.
 */
public class LivingEntityAttackEvents {

	/**
	 * This event is invoked when a {@link LivingEntity} attacks another
	 *     <strong>before</strong> the damage is dealt.
	 * <p>
	 * This event is cancellable by calling {@link DamageContext#cancel()}.
	 * <p>
	 * Implementations should not assume the hit will go through. Ideally, this
	 * event is used for conditionally cancelling or altering the damage value.
	 */
	public static final Event<Before> BEFORE = Event.create(Before.class,
			callbacks -> context -> {
				for (Before callback : callbacks) {
					callback.beforeDamage(context);

					if (context.isCanceled()) return;
				}
			});

	/**
	 * This event is invoked when a {@link LivingEntity} attacks another
	 *     <strong>after</strong> the damage is dealt.
	 */
	public static final Event<After> AFTER = Event.create(After.class,
			callbacks -> (attacker, stack, target, source, damage) -> {
				for (After callback : callbacks) {
					callback.afterDamage(attacker, stack, target, source, damage);
				}
			});



	@FunctionalInterface
	public interface Before {
		/**
		 * Invoked <strong>before</strong> a {@link LivingEntity} damages another.
		 *
		 * @param context the {@link DamageContext} containing all the relevant arguments
		 */
		void beforeDamage(DamageContext context);
	}

	@FunctionalInterface
	public interface After {
		/**
		 * Invoked <strong>after</strong> a {@link LivingEntity} damages another.
		 *
		 * @param attacker the attacking entity
		 * @param stack the {@link ItemStack} in the attacker's main-hand
		 * @param target the target {@link LivingEntity}
		 * @param source the {@link DamageSource} of the attack
		 * @param damage the damage that was dealt
		 */
		void afterDamage(LivingEntity attacker, ItemStack stack, LivingEntity target, DamageSource source, float damage);
	}
}
