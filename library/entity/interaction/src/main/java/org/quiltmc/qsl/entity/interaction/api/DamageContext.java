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
import net.minecraft.item.ItemStack;

/**
 * An encapsulation of several arguments relevant to a damage context with a
 *     mutable damage value and cancellation that is shared across all listeners.
 */
public class DamageContext {

	private final LivingEntity attacker;
	private final ItemStack stack;
	private final Entity target;
	private final DamageSource source;
	private float damage;
	private boolean canceled = false;

	public DamageContext(LivingEntity attacker, ItemStack stack, Entity target, DamageSource source, float damage) {
		this.attacker = attacker;
		this.stack = stack;
		this.target = target;
		this.source = source;
		this.damage = damage;
	}

	/**
	 * Gets the attacking {@link LivingEntity} in the damage context.
	 * @return the attacking {@link LivingEntity}
	 */
	public LivingEntity getAttacker() {
		return this.attacker;
	}

	/**
	 * Gets the {@link ItemStack} in the attacker's main-hand.
	 * @return the {@link ItemStack} used by the attacker
	 */
	public ItemStack getWeapon() {
		return this.stack;
	}

	/**
	 * Gets the targeted {@link Entity} in the damage context.
	 * @return the targeted {@link Entity}
	 */
	public Entity getTarget() {
		return this.target;
	}

	/**
	 * Gets the {@link DamageSource} used in the damage context.
	 * @return the damage's {@link DamageSource}
	 */
	public DamageSource getDamageSource() {
		return source;
	}

	/**
	 * Gets the damage amount in the damage context.
	 * @return the damage amount
	 */
	public float getDamage() {
		return damage;
	}

	/**
	 * Sets the damage amount in the damage context.
	 * @param damage the desired damage value
	 */
	public void setDamage(float damage) {
		this.damage = damage;
	}

	/**
	 * Cancels the damage event.
	 */
	public void cancel() {
		this.canceled = true;
	}

	/**
	 * Returns whether the event has been canceled or not.
	 * @return {@code true} if the event has been canceled, otherwise {@code false}
	 */
	public boolean isCanceled() {
		return this.canceled;
	}
}
