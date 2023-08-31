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

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.quiltmc.qsl.entity.effect.api.QuiltStatusEffectExtensions;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin implements QuiltStatusEffectExtensions {
	@Shadow
	public abstract void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier);

	@Override
	public void onRemoved(@NotNull LivingEntity entity, @NotNull AttributeContainer attributes, @NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason) {
		this.onRemoved(entity, attributes, effect.getAmplifier());
	}
}
