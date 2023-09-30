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

package org.quiltmc.qsl.item.extensions.api.bow;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.ParameterInvokingEvent;

public final class BowShotProjectileEvents {
	/**
	 * This event modifies the projectile entity. Returning a new entity will not change the entity that is spawned in.
	 */
	@ParameterInvokingEvent
	public static final Event<ModifyProjectileFromBow> BOW_MODIFY_SHOT_PROJECTILE = Event.create(ModifyProjectileFromBow.class,
			callbacks -> (bowStack, arrowStack, user, pullProgress, projectile) -> {
				if (bowStack.getItem() instanceof ModifyProjectileFromBow callback) {
					callback.modifyProjectileShot(bowStack, arrowStack, user, pullProgress, projectile);
				}

				for (var callback : callbacks) {
					callback.modifyProjectileShot(bowStack, arrowStack, user, pullProgress, projectile);
				}
			});

	/**
	 * This event replaces the projectile entity. Any modifications done in this step without returning a new entity can be erased.
	 * Do not use this event to only modify the arrow entity, as {@link BowShotProjectileEvents#BOW_MODIFY_SHOT_PROJECTILE} is the proper event.
	 */
	public static final Event<ReplaceProjectileFromBow> BOW_REPLACE_SHOT_PROJECTILE = Event.create(ReplaceProjectileFromBow.class,
			callbacks -> (bowStack, arrowStack, user, pullProgress, projectile) -> {
				for (var callback : callbacks) {
					PersistentProjectileEntity replacedEntity = callback.replaceProjectileShot(bowStack, arrowStack, user, pullProgress, projectile);

					if (replacedEntity != null) {
						return replacedEntity;
					}
				}

				return projectile;
			});

	private BowShotProjectileEvents() {}

	public interface ReplaceProjectileFromBow {
		/**
		 * In this method you can replace the arrow shot from your custom bow. Applies all the vanilla arrow modifiers first.
		 *
		 * @param bowStack     the item stack for the {@link net.minecraft.item.BowItem}
		 * @param arrowStack   the item stack for the arrows
		 * @param user         the user of the bow
		 * @param pullProgress the pull progress of the bow from {@code 0.0} to {@code 1.0}
		 * @param projectile   the arrow entity to be spawned
		 * @return the arrow entity, either new or {@code null} to signify no changes occurred
		 */
		@Nullable PersistentProjectileEntity replaceProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user,
				@Range(from = 0, to = 1) float pullProgress, PersistentProjectileEntity projectile);
	}

	public interface ModifyProjectileFromBow {
		/**
		 * In this method you can modify the behavior of arrows shot from your custom bow. Applies all the vanilla arrow modifiers first.
		 *
		 * @param bowStack     the item stack for the {@link net.minecraft.item.BowItem}
		 * @param arrowStack   the item stack for the arrows
		 * @param user         the user of the bow
		 * @param pullProgress the pull progress of the bow from {@code 0.0} to {@code 1.0}
		 * @param projectile   the arrow entity to be spawned
		 */
		void modifyProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, @Range(from = 0, to = 1) float pullProgress,
				PersistentProjectileEntity projectile);
	}
}
