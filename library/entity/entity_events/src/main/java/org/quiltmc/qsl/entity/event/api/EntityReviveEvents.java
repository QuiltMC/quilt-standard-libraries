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
 * A pair of callbacks which are invoked when a LivingEntity takes fatal damage.
 * <p>
 * Mods can return true from a callback to keep the entity alive, like totems of undying do. This will also prevent
 * {@link LivingEntityDeathCallback} listeners from being called.
 * <p>
 * Vanilla checks for entity health {@code <= 0} each tick (with
 * {@link net.minecraft.entity.LivingEntity#isDead()}), and kills the entity if this is true - so the entity will
 * still die next tick if this event is cancelled. It's assumed that the listener will do something to prevent this,
 * for example:
 * <ul>
 *     <li>a totem-of-undying mod giving the player a little health + regeneration effect</li>
 *     <li>a minigame mod teleporting the player into a 'respawn room' and setting their health to 20.0</li>
 *     <li>a mod that changes death mechanics switching the player over to the mod's play-mode, where death doesn't
 *     apply</li>
 * </ul>
 * <p>
 * These events are short-circuiting - if the entity has already been revived, your callback will not fire.
 */
public final class EntityReviveEvents {
	/**
	 * Called before totems try to revive the entity.
	 */
	public static Event<TryReviveBeforeTotem> BEFORE_TOTEM = Event.create(TryReviveBeforeTotem.class, callbacks -> (entity, damageSource) -> {
		for (var callback : callbacks) {
			if (callback.tryReviveBeforeTotem(entity, damageSource)) {
				return true;
			}
		}

		return false;
	});

	/**
	 * Called after there has been an attempt to revive with totems, but it has not been successful.
	 */
	public static Event<TryReviveAfterTotem> AFTER_TOTEM = Event.create(TryReviveAfterTotem.class, callbacks -> (entity, damageSource) -> {
		for (var callback : callbacks) {
			if (callback.tryReviveAfterTotem(entity, damageSource)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface TryReviveBeforeTotem extends EventAwareListener {
		/**
		 * Determines whether an entity should be revived.
		 *
		 * @param entity       the entity
		 * @param damageSource the fatal damage source
		 * @return {@code true} if the entity which has fatal damage should be revived, or {@code false} otherwise
		 */
		boolean tryReviveBeforeTotem(LivingEntity entity, DamageSource damageSource);
	}

	@FunctionalInterface
	public interface TryReviveAfterTotem extends EventAwareListener {
		/**
		 * Determines whether an entity should be revived.
		 *
		 * @param entity       the entity
		 * @param damageSource the fatal damage source
		 * @return {@code true} if the entity which has fatal damage should be revived, or {@code false} otherwise
		 */
		boolean tryReviveAfterTotem(LivingEntity entity, DamageSource damageSource);
	}

	private EntityReviveEvents() {}
}
