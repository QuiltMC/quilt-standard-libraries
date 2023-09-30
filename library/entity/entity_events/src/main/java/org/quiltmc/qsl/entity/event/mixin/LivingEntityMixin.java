/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.entity.event.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import org.quiltmc.qsl.entity.event.api.EntityReviveEvents;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
	private void invokeTryReviveBeforeTotemEvent(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (EntityReviveEvents.BEFORE_TOTEM.invoker().tryReviveBeforeTotem((LivingEntity) (Object) this, source)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "tryUseTotem", at = @At("RETURN"), cancellable = true)
	private void invokeTryReviveAfterTotemEvent(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			cir.setReturnValue(EntityReviveEvents.AFTER_TOTEM.invoker().tryReviveAfterTotem((LivingEntity) (Object) this, source));
		}
	}
}
