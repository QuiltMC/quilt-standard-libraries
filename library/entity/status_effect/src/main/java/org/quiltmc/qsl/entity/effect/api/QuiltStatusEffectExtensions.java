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

package org.quiltmc.qsl.entity.effect.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(StatusEffect.class)
public interface QuiltStatusEffectExtensions {
	default boolean shouldRemove(@NotNull LivingEntity entity, @NotNull StatusEffectInstance instance, @NotNull StatusEffectRemovalReason reason) {
		return reason.removesEffectType(instance.getEffectType());
	}

	/**
	 * Called <em>after</em> {@link StatusEffect#onRemoved(LivingEntity, AttributeContainer, int)}.
	 */
	default void onRemoved(@NotNull LivingEntity entity, @NotNull AttributeContainer attributes, int amplifier,
			@NotNull StatusEffectRemovalReason reason) {}
}
