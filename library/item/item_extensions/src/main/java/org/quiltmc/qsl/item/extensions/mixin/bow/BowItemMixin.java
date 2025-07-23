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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;
import org.quiltmc.qsl.item.extensions.impl.BowAttackModificationImpl;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends RangedWeaponItemMixin implements BowExtensions {
	// Modifies the pull progress if a custom bow is used
	@Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
	private float redirectPullProgress(int useTicks, ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks) {
		return this.getCustomPullProgress(useTicks, bowStack);
	}

	// Overrides stub handler from RangedWeaponItemMixin
	// Allows custom bows to modify the projectile shot by bows
	@Override
	protected ProjectileEntity modifyArrow(
			ProjectileEntity original, ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float speed
	) {
		if (original instanceof PersistentProjectileEntity persistentProjectile) {
			// speed is calculated from pullProgress * 3 in BowItem::onStoppedUsing
			float pullProgress = speed / 3f;

			return BowAttackModificationImpl.modifyShotProjectile(
				persistentProjectile,
				arrowStack, pullProgress, bowStack, user
			);
		} else {
			return original;
		}
	}
}
