/*
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

package org.quiltmc.qsl.item.extensions.api.crossbow;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.ParameterInvokingEvent;

public final class CrossbowShotProjectileEvents {
	/**
	 * This event modifies the projectile entity shot from a crossbow.
	 */
	@ParameterInvokingEvent
	public static final Event<ModifyProjectileFromCrossbow> CROSSBOW_MODIFY_SHOT_PROJECTILE = Event.create(ModifyProjectileFromCrossbow.class,
			callbacks -> (bowStack, projectileStack, user, projectile) -> {
				if (bowStack.getItem() instanceof ModifyProjectileFromCrossbow callback) {
					callback.modifyProjectileShot(bowStack, projectileStack, user, projectile);
				}

				for (var callback : callbacks) {
					callback.modifyProjectileShot(bowStack, projectileStack, user, projectile);
				}
			});

	/**
	 * This event replaces the projectile entity shot from a crossbow. Any modifications done in this step without returning a new entity can be erased.
	 * Do not use this event to only modify the arrow entity, as {@link CrossbowShotProjectileEvents#CROSSBOW_MODIFY_SHOT_PROJECTILE} is the proper event.
	 */
	public static final Event<ReplaceProjectileFromCrossbow> CROSSBOW_REPLACE_SHOT_PROJECTILE = Event.create(ReplaceProjectileFromCrossbow.class,
			callbacks -> (bowStack, projectileStack, user, projectile) -> {
				for (var callback : callbacks) {
					PersistentProjectileEntity replacedEntity = callback.replaceProjectileShot(bowStack, projectileStack, user, projectile);

					if (replacedEntity != null) {
						return replacedEntity;
					}
				}

				return projectile;
			});

	private CrossbowShotProjectileEvents() {}

	public interface ReplaceProjectileFromCrossbow {
		/**
		 * In this method you can replace the arrow shot from your custom crossbow. Applies all the vanilla arrow modifiers first.
		 *
		 * @param crossbowStack   the item stack for the {@link net.minecraft.item.CrossbowItem}
		 * @param projectileStack the item stack for the projectile currently being shot
		 * @param user            the user of the crossbow
		 * @param projectile      the arrow entity to be spawned
		 * @return the new projectile entity, or {@code null} if you do not change the entity
		 */
		PersistentProjectileEntity replaceProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity user,
				@NotNull PersistentProjectileEntity projectile);
	}

	public interface ModifyProjectileFromCrossbow {
		/**
		 * In this method you can modify the behavior of arrows shot from your custom crossbow. Applies all the vanilla arrow modifiers first.
		 *
		 * @param crossbowStack   the item stack for the {@link net.minecraft.item.CrossbowItem}
		 * @param projectileStack the item stack for the projectile currently being shot
		 * @param user            the user of the crossbow
		 * @param projectile      the arrow entity to be spawned
		 */
		void modifyProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity user, @NotNull PersistentProjectileEntity projectile);
	}
}
