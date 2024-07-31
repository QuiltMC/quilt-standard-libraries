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

package org.quiltmc.qsl.item.extensions.mixin.crossbow;

import java.util.Objects;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extensions.api.crossbow.CrossbowExtensions;
import org.quiltmc.qsl.item.extensions.api.crossbow.CrossbowShotProjectileEvents;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin implements CrossbowExtensions {
	@Inject(method = "getProjectile", at = @At(value = "RETURN"), cancellable = true)
	private void createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir) {
		ProjectileEntity projectileEntity = CrossbowShotProjectileEvents.CROSSBOW_REPLACE_SHOT_PROJECTILE.invoker().replaceProjectileShot(crossbow, projectileStack, entity, cir.getReturnValue());
		CrossbowShotProjectileEvents.CROSSBOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(crossbow, projectileStack, entity, projectileEntity);
		cir.setReturnValue(projectileEntity);
	}

	// Redirecting this method in order to get the item stack and shooting entity
	@WrapOperation(
			method = "use",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FFLnet/minecraft/entity/LivingEntity;)V"
			)
	)
	private void shootAll(CrossbowItem instance, World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence, LivingEntity livingEntity, Operation<Void> original) {
		float newSpeed = stack.getItem() instanceof CrossbowExtensions crossbowItem ? crossbowItem.getProjectileSpeed(stack, Objects.requireNonNull(stack.get(DataComponentTypes.CHARGED_PROJECTILES)), entity) : speed;
		original.call(instance, world, entity, hand, stack, newSpeed, divergence, livingEntity);
	}
}
