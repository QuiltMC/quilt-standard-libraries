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
import net.minecraft.item.ItemStack;
import net.minecraft.item.RemoveEffectsComponent;
import net.minecraft.registry.Holder;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;
import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;

// See LivingEntityMixin
@Mixin(value = RemoveEffectsComponent.class, priority = QuiltStatusEffectInternals.MIXIN_PRIORITY)
public abstract class RemoveEffectsComponentMixin {
	@WrapOperation(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;removeStatusEffect(Lnet/minecraft/registry/Holder;)Z"))
	private boolean quilt$addRemovalReason(
			LivingEntity instance, Holder<StatusEffect> effect, Operation<Boolean> original, World world, ItemStack stack
	) {
		return instance.removeStatusEffect(
			effect, new StatusEffectRemovalReason.RemoveEffectsComponentReason(stack, ((RemoveEffectsComponent) (Object) this))
		);
	}
}
