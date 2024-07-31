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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;

@Mixin(RangedWeaponItem.class)
public abstract class RangedWeaponItemMixin {
	@Unique
	private final ThreadLocal<PersistentProjectileEntity> quilt$onStoppedUsing$shotProjectile = new ThreadLocal<>();

	// Allows custom bows to modify the projectile shot by bows
	// Two mixins are needed for this in order to capture the locals
	@Inject(
			method = "shootAll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void onStoppedUsing_modifyArrow(
			ServerWorld world, LivingEntity user, Hand hand, ItemStack bowStack,
			List<ItemStack> list, float pullProgress, float g, boolean bl,
			LivingEntity livingEntity2, CallbackInfo ci, float h, float i,
			float j, float k, int l, ItemStack arrowStack,
			float m, ProjectileEntity projectileEntity) {
		RangedWeaponItem self = (RangedWeaponItem) (Object) this;
		if ((self instanceof BowItem)) {
			this.quilt$onStoppedUsing$shotProjectile.set(BowShotProjectileEvents.BOW_REPLACE_SHOT_PROJECTILE.invoker().replaceProjectileShot(bowStack, arrowStack, user, pullProgress / 3f, (PersistentProjectileEntity) projectileEntity));
			BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(bowStack, arrowStack, user, pullProgress / 3f, this.quilt$onStoppedUsing$shotProjectile.get());
		}
	}

	// Actually modifies the projectile
	@ModifyVariable(
			method = "shootAll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
	)
	public ProjectileEntity onStoppedUsing_replaceArrow(ProjectileEntity persistentProjectileEntity) {
		return this.quilt$onStoppedUsing$shotProjectile.get();
	}

	// Removes the pointer to the shot projectile for GC
	@Inject(
			method = "shootAll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER)
	)
	public void onStoppedUsing_resetInternalProjectile(
			ServerWorld world, LivingEntity livingEntity,
			Hand hand, ItemStack stack, List<ItemStack> list,
			float f, float g, boolean bl,
			LivingEntity livingEntity2, CallbackInfo ci) {
		this.quilt$onStoppedUsing$shotProjectile.remove();
	}
}
