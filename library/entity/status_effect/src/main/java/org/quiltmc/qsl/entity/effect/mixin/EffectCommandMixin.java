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

package org.quiltmc.qsl.entity.effect.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.registry.Holder;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;
import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;

// See LivingEntityMixin
@Mixin(value = EffectCommand.class, priority = QuiltStatusEffectInternals.MIXIN_PRIORITY)
public abstract class EffectCommandMixin {
	@WrapOperation(
			method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z")
	)
	private static boolean quilt$addRemovalReason(LivingEntity instance, Operation<Boolean> original) {
		return instance.clearStatusEffects(StatusEffectRemovalReason.COMMAND_ALL) > 0;
	}

	@WrapOperation(
			method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/registry/Holder;)I",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;removeStatusEffect(Lnet/minecraft/registry/Holder;)Z"
			)
	)
	private static boolean quilt$addRemovalReason(LivingEntity instance, Holder<StatusEffect> type, Operation<Boolean> original) {
		return instance.removeStatusEffect(type, StatusEffectRemovalReason.COMMAND_ONE);
	}
}
