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

import org.quiltmc.qsl.base.api.util.TriState;

/**
 * Utilities for dealing with status effects.
 */
public final class StatusEffectUtils {
	private StatusEffectUtils() {
		throw new UnsupportedOperationException("StatusEffectUtils only contains static definitions.");
	}

	/**
	 * Checks if the status effect should be removed or not.
	 *
	 * @param entity the entity that has the status effect
	 * @param effect the status effect
	 * @param reason the reason the status effect should be removed
	 * @return {@code true} if the status effect should be removed, or {@code false} otherwise.
	 *
	 * @see StatusEffectEvents#SHOULD_REMOVE
	 * @see StatusEffect#shouldRemove(LivingEntity, StatusEffectInstance, StatusEffectRemovalReason)
	 */
	public static boolean shouldRemove(@NotNull LivingEntity entity, @NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason) {
		TriState eventResult = StatusEffectEvents.SHOULD_REMOVE.invoker().shouldRemove(entity, effect, reason);
		if (eventResult == TriState.DEFAULT) {
			return effect.getEffectType().shouldRemove(entity, effect, reason);
		} else {
			return eventResult.toBooleanOrElse(false);
		}
	}
}
