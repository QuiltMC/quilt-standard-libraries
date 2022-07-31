/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.item.extension.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.quiltmc.qsl.item.extension.api.ShotProjectileEvents;
import org.quiltmc.qsl.item.extension.impl.BowExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public class BowItemMixin implements BowExtensions {
	@Unique
	private final ThreadLocal<PersistentProjectileEntity> quilt$onStoppedUsing$shotProjectile = new ThreadLocal<>();

	// Allows custom bows to modify the projectile shot by bows
	// Two mixins are needed for this in order to capture the locals
	@Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStoppedUsing_modifyArrow(ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo info, PlayerEntity playerEntity, boolean bl, ItemStack arrowStack, int i, float pullProgress, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
		quilt$onStoppedUsing$shotProjectile.set(ShotProjectileEvents.BOW_REPLACE_SHOT_PROJECTILE.invoker().replaceProjectileShot(bowStack, arrowStack, user, pullProgress, persistentProjectileEntity));
		ShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(bowStack, arrowStack, user, pullProgress, quilt$onStoppedUsing$shotProjectile.get());
	}

	// Actually modifies the projectile
	@ModifyVariable(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	public PersistentProjectileEntity onStoppedUsing_replaceArrow(PersistentProjectileEntity persistentProjectileEntity) {
		return quilt$onStoppedUsing$shotProjectile.get();
	}

	// Removes the pointer to the shot projectile for GC
	@Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER))
	public void onStoppedUsing_resetInternalProjectile(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		quilt$onStoppedUsing$shotProjectile.remove();
	}

	// Modifies the pull progress if a custom bow is used
	@Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
	private float redirectPullProgress(int useTicks, ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks) {
		return this.getCustomPullProgress(useTicks, bowStack);
	}
}
