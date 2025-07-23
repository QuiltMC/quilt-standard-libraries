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
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;

@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin {
	// stub handler to be overriden by BowItemMixin
	@ModifyExpressionValue(
			method = "shootAll",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/item/RangedWeaponItem;getProjectile(Lnet/minecraft/world/World;"
					+ "Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;"
					+ "Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/projectile/ProjectileEntity;"
			)
	)
	protected ProjectileEntity modifyArrow(
			ProjectileEntity original, @Local(ordinal = 0, argsOnly = true) ItemStack bowStack,
			@Local(ordinal = 1) ItemStack arrowStack, @Local(ordinal = 0, argsOnly = true) LivingEntity user,
			@Local(ordinal = 0, argsOnly = true) float speed
	) {
		return original;
	}
}
