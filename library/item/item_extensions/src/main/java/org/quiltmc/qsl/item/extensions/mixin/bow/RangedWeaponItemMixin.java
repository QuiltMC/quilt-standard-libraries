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

package org.quiltmc.qsl.item.extensions.mixin.bow;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;

import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;

@Mixin(RangedWeaponItem.class)
public abstract class RangedWeaponItemMixin {
	// Allows custom bows to modify the projectile shot by bows
	@ModifyExpressionValue(
		method = "shootAll",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/RangedWeaponItem;getProjectile(Lnet/minecraft/world/World;" +
				"Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)" +
				"Lnet/minecraft/entity/projectile/ProjectileEntity;"
		)
	)
	public ProjectileEntity modifyArrow(ProjectileEntity original, @Local(ordinal = 0, argsOnly = true) ItemStack bowStack, @Local(ordinal = 1) ItemStack arrowStack, @Local(ordinal = 0, argsOnly = true) LivingEntity user, @Local(ordinal = 0, argsOnly = true) float speed) {
		if (original instanceof PersistentProjectileEntity persistentProjectile) {
			// speed is calculated from pullProgress * 3 in BowItem::onStoppedUsing
			final float pullProgress = speed / 3f;

			final PersistentProjectileEntity projectile = BowShotProjectileEvents.BOW_REPLACE_SHOT_PROJECTILE.invoker()
				.replaceProjectileShot(
					bowStack, arrowStack, user, pullProgress,
					persistentProjectile
				);

			BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(
				bowStack, arrowStack, user, pullProgress, persistentProjectile
			);

			return projectile;
		} else {
			return original;
		}
	}
}
