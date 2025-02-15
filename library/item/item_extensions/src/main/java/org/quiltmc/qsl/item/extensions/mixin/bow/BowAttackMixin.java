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

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;

// Will need to be updated if more bow-attacking mobs are added
@Mixin({AbstractSkeletonEntity.class, IllusionerEntity.class})
public abstract class BowAttackMixin extends MobEntity implements RangedAttackMob {
	protected BowAttackMixin(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

	// fixed target reference & method signature
	// beyond that i'm not touching this one either
	// FIXME: return type is now <T extends ProjectileEntity> and targets
	//  ProjectileEntity#spawn as opposed to world#spawnEntity
	//  furthermore, pullProgress is no longer a thing, so uhm.
	//  good luck sorting that out


	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;DDDFF)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
	public <T extends ProjectileEntity> T modifyShotProjectile(T projectileEntity, ServerWorld world, ItemStack stack, double x, double y, double z, float power, float divergence) {
		ItemStack bowStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
		ItemStack arrowStack = this.getArrowType(bowStack);

		PersistentProjectileEntity replacedPersistentProjectileEntity = BowShotProjectileEvents.BOW_REPLACE_SHOT_PROJECTILE.invoker().replaceProjectileShot(bowStack, arrowStack, this, pullProgress, (PersistentProjectileEntity) persistentProjectileEntity);
		BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(bowStack, arrowStack, this, pullProgress, replacedPersistentProjectileEntity);

		return world.spawnEntity(replacedPersistentProjectileEntity);
	}
}
