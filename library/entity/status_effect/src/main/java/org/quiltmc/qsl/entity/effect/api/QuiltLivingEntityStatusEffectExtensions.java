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

package org.quiltmc.qsl.entity.effect.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Holder;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Provides status effect-related extensions to the {@link LivingEntity} class.
 */
@InjectedInterface(LivingEntity.class)
public interface QuiltLivingEntityStatusEffectExtensions {
	/**
	 * Removes a status effect of a specific type.
	 *
	 * @param type   the type of status effect to remove
	 * @param reason the reason to remove the status effect
	 * @return {@code true} if the status effect was successfully removed, or {@code false} otherwise.
	 */
	default boolean removeStatusEffect(@NotNull Holder<StatusEffect> type, @NotNull StatusEffectRemovalReason reason) {
		throw new UnsupportedOperationException("No implementation of removeStatusEffect could be found.");
	}

	/**
	 * Removes all status effects.
	 *
	 * @param reason the reason to remove the status effects
	 * @return the number of status effects that were successfully removed.
	 */
	default int clearStatusEffects(@NotNull StatusEffectRemovalReason reason) {
		throw new UnsupportedOperationException("No implementation of clearStatusEffects could be found.");
	}

	/**
	 * Should be called when a status effect is removed.
	 *
	 * @param effect the effect that was removed
	 * @param reason the reason the effect was removed
	 */
	default void onStatusEffectRemoved(@NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason) {
		throw new UnsupportedOperationException("No implementation of onStatusEffectRemoved could be found.");
	}
}
