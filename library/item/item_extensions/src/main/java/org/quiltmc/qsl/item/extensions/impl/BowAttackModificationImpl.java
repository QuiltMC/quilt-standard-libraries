/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.item.extensions.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;

@ApiStatus.Internal
public class BowAttackModificationImpl {
	public static PersistentProjectileEntity modifyShotProjectile(
			PersistentProjectileEntity originalProjectile,
			ItemStack arrowStack, float pullProgress, ItemStack bowStack, LivingEntity user
	) {
		final PersistentProjectileEntity replacedPersistentProjectileEntity = BowShotProjectileEvents
				.BOW_REPLACE_SHOT_PROJECTILE.invoker()
				.replaceProjectileShot(bowStack, arrowStack, user, pullProgress, originalProjectile);

		BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(
				bowStack, arrowStack, user, pullProgress, replacedPersistentProjectileEntity
		);

		return replacedPersistentProjectileEntity;
	}
}
