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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.EffectCommand;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@Mixin(EffectCommand.class)
public abstract class EffectCommandMixin {
	@Redirect(
			method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z")
	)
	private static boolean quilt$addRemovalReason(LivingEntity instance) {
		return instance.clearStatusEffects(StatusEffectRemovalReason.COMMAND_ALL) > 0;
	}

	@Redirect(
			method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/registry/Holder;)I",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;removeStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"
			)
	)
	private static boolean quilt$addRemovalReason(LivingEntity instance, StatusEffect type) {
		return instance.removeStatusEffect(type, StatusEffectRemovalReason.COMMAND_ONE);
	}
}
