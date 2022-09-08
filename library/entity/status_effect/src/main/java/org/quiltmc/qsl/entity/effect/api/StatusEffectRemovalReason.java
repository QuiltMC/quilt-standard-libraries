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

import net.minecraft.entity.effect.StatusEffect;

@FunctionalInterface
public interface StatusEffectRemovalReason {
	/**
	 * Try to minimize use of this.
	 */
	StatusEffectRemovalReason UNKNOWN = () -> "unknown";

	StatusEffectRemovalReason GENERIC_ALL = () -> "generic.all";

	StatusEffectRemovalReason GENERIC_SPECIFIC = () -> "generic.specific";

	StatusEffectRemovalReason COMMAND_ALL = () -> "command.all";

	StatusEffectRemovalReason COMMAND_SPECIFIC = () -> "command.specific";

	StatusEffectRemovalReason DRANK_MILK = () -> "action.drank_milk";

	String getName();

	/**
	 * Checks if the removal reason should remove effects of this type. Note that the status effect ultimately
	 * has the final say on whether it's removed or not.
	 * <p>
	 * Override this, for example, to make a status effect remover that only removes
	 * {@linkplain net.minecraft.entity.effect.StatusEffectType#HARMFUL harmful effects}.
	 *
	 * @param type the effect type to check
	 * @return {@code true} if effects of this type should be removed, {@code false} otherwise.
	 */
	default boolean removesEffectType(StatusEffect type) {
		return true;
	}
}
