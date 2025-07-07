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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ClearAllEffectsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;
import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;

// See LivingEntityMixin
@Mixin(value = ClearAllEffectsComponent.class, priority = QuiltStatusEffectInternals.MIXIN_PRIORITY)
public abstract class ClearAllEffectsComponentMixin {
	@Inject(method = "apply", at = @At(value = "HEAD"))
	private void quilt$addRemovalReason(
			World world, ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> cir
	) {
		entity.clearStatusEffects(StatusEffectRemovalReason.DRANK_MILK);
	}
}
