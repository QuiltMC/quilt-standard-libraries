/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.enchantment.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.ai.goal.SkeletonHorseTrapTriggerGoal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.world.LocalDifficulty;

import org.quiltmc.qsl.enchantment.api.EntityEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantmentHelper;

@Mixin(SkeletonHorseTrapTriggerGoal.class)
public abstract class SkeletonHorseTrapTriggerGoalMixin {
	@Inject(method = "getSkeleton", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SkeletonEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void setEntityEnchantingContext(LocalDifficulty localDifficulty, HorseBaseEntity vehicle, CallbackInfoReturnable<SkeletonEntity> cir, SkeletonEntity skeleton) {
		QuiltEnchantmentHelper.setContext(new EntityEnchantingContext<>(0, 0, null, vehicle.world, skeleton.getRandom(), false, skeleton));
	}

	@Inject(method = "getSkeleton", at = @At("RETURN"))
	private void clearEntityEnchantingContext(LocalDifficulty localDifficulty, HorseBaseEntity vehicle, CallbackInfoReturnable<SkeletonEntity> cir) {
		QuiltEnchantmentHelper.clearContext();
	}
}
