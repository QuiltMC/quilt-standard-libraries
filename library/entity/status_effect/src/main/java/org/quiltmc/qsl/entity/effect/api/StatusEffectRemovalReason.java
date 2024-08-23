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
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;

/**
 * Represents a reason for a {@link StatusEffectInstance} to be removed.
 */
public class StatusEffectRemovalReason {
	/**
	 * Used when an effect expires and is removed.
	 * <p>
	 * Cannot be cancelled ({@link StatusEffect#shouldRemove(LivingEntity, StatusEffectInstance, StatusEffectRemovalReason)} is not called for this reason).
	 */
	public static final StatusEffectRemovalReason EXPIRED = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("expired"));

	/**
	 * Used when an effect is removed to reapply it during an upgrade.
	 * <p>
	 * Cannot be cancelled ({@link StatusEffect#shouldRemove(LivingEntity, StatusEffectInstance, StatusEffectRemovalReason)} is not called for this reason).
	 */
	public static final StatusEffectRemovalReason UPGRADE_REAPPLYING = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("upgrade.reapplying"));

	/**
	 * Used when effects are removed via the vanilla {@link LivingEntity#clearStatusEffects()} method.
	 */
	public static final StatusEffectRemovalReason GENERIC_ALL = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("generic.all"));

	/**
	 * Used when an effect is removed via the vanilla {@link LivingEntity#removeStatusEffect(net.minecraft.registry.Holder) LivingEntity#removeStatusEffect(Holder&lt;StatusEffect&gt;)} method.
	 */
	public static final StatusEffectRemovalReason GENERIC_ONE = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("generic.one"));

	/**
	 * Used when effects are removed via the {@code /effect clear} command.
	 */
	public static final StatusEffectRemovalReason COMMAND_ALL = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("command.all"));

	/**
	 * Used when an effect is removed via the {@code /effect remove} command.
	 */
	public static final StatusEffectRemovalReason COMMAND_ONE = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("command.one"));

	/**
	 * Used when effects are removed via drinking milk. Does <em>not</em> have to be the vanilla milk bucket.
	 */
	public static final StatusEffectRemovalReason DRANK_MILK = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("action.drank_milk"));

	protected final @NotNull Identifier id;

	/**
	 * Creates a new {@code StatusEffectRemovalReason}.
	 *
	 * @param id the removal reason's identifier
	 */
	public StatusEffectRemovalReason(@NotNull Identifier id) {
		this.id = id;
	}

	/**
	 * {@return this reason's identifier}
	 */
	public @NotNull Identifier getId() {
		return this.id;
	}

	/**
	 * Checks if the removal reason should remove this effect. Note that the status effect ultimately
	 * has the final say on whether it's removed or not, and this is simply used as a hint.
	 * <p>
	 * Override this, for example, to make a status effect remover that only removes
	 * {@linkplain net.minecraft.entity.effect.StatusEffectType#HARMFUL harmful effects}.
	 *
	 * @param effect the effect type to check
	 * @return {@code true} if effects of this type should be removed, or {@code false} otherwise.
	 */
	public boolean removesEffect(StatusEffectInstance effect) {
		return true;
	}
}
