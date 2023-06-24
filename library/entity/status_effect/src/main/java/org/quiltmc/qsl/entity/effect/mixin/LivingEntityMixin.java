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

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.quiltmc.qsl.entity.effect.api.QuiltLivingEntityStatusEffectExtensions;
import org.quiltmc.qsl.entity.effect.api.StatusEffectEvents;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;
import org.quiltmc.qsl.entity.effect.api.StatusEffectUtils;
import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements QuiltLivingEntityStatusEffectExtensions {
	@SuppressWarnings("ConstantConditions")
	public LivingEntityMixin() {
		super(null, null);
	}

	@Shadow
	@Final
	private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

	@Shadow
	protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

	@Unique
	private StatusEffectRemovalReason quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean removeStatusEffect(@NotNull StatusEffect type, @NotNull StatusEffectRemovalReason reason) {
		var effect = this.activeStatusEffects.get(type);
		if (effect == null) {
			return false;
		}

		if (StatusEffectUtils.shouldRemove((LivingEntity) (Object) this, effect, reason)) {
			this.activeStatusEffects.remove(type);
			this.onStatusEffectRemoved(effect, reason);
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearStatusEffects(@NotNull StatusEffectRemovalReason reason) {
		if (this.getWorld().isClient) {
			return 0;
		}

		int removed = 0;
		var it = this.activeStatusEffects.values().iterator();
		while (it.hasNext()) {
			var effect = it.next();
			if (StatusEffectUtils.shouldRemove((LivingEntity) (Object) this, effect, reason)) {
				it.remove();
				this.onStatusEffectRemoved(effect, reason);
				removed++;
			}
		}

		return removed;
	}

	@Override
	public void onStatusEffectRemoved(@NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason) {
		this.quilt$lastRemovalReason = reason;
		this.onStatusEffectRemoved(effect);
		this.quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(
			method = "onStatusEffectApplied",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
					shift = At.Shift.AFTER
			)
	)
	private void quilt$callOnAppliedEvent(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, false);
	}

	@Redirect(
			method = "onStatusEffectRemoved",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V"
			)
	)
	private void quilt$callOnRemovedWithReason(StatusEffect instance, LivingEntity entity, AttributeContainer attributes, int amplifier,
			StatusEffectInstance effect) {
		instance.onRemoved(entity, attributes, effect, this.quilt$lastRemovalReason);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved(entity, effect, this.quilt$lastRemovalReason);
	}

	/**
	 * @author The Quilt Project
	 * @reason Adding removal reason
	 */
	@Overwrite
	public boolean removeStatusEffect(StatusEffect type) {
		return this.removeStatusEffect(type, StatusEffectRemovalReason.GENERIC_ONE);
	}

	/**
	 * @author The Quilt Project
	 * @reason Adding removal reason
	 */
	@Overwrite
	public boolean clearStatusEffects() {
		return this.clearStatusEffects(StatusEffectRemovalReason.GENERIC_ALL) > 0;
	}

	@Redirect(method = "tickStatusEffects", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V")
	)
	private void quilt$removeWithExpiredReason(LivingEntity instance, StatusEffectInstance effect) {
		instance.onStatusEffectRemoved(effect, StatusEffectRemovalReason.EXPIRED);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved(instance, effect, StatusEffectRemovalReason.EXPIRED);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(
			method = "onStatusEffectUpgraded",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
				shift = At.Shift.AFTER
			)
	)
	private void quilt$callOnAppliedEvent_upgradeReapplying(StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, true);
	}

	@Redirect(method = "onStatusEffectUpgraded", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V")
	)
	private void quilt$removeWithUpgradeApplyingReason(StatusEffect instance, LivingEntity entity, AttributeContainer attributes, int amplifier,
			StatusEffectInstance effect) {
		instance.onRemoved(entity, attributes, effect, StatusEffectRemovalReason.UPGRADE_REAPPLYING);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved(entity, effect, StatusEffectRemovalReason.UPGRADE_REAPPLYING);
	}
}
