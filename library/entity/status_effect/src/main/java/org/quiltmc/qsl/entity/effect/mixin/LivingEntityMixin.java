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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Holder;

import org.quiltmc.qsl.entity.effect.api.QuiltLivingEntityStatusEffectExtensions;
import org.quiltmc.qsl.entity.effect.api.StatusEffectEvents;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;
import org.quiltmc.qsl.entity.effect.api.StatusEffectUtils;
import org.quiltmc.qsl.entity.effect.impl.QuiltStatusEffectInternals;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// We want to make sure that our wrap operations are put before other mods, so that we wrap the vanilla call and not a mod's call.
// This is because we do not call the vanilla method, so any mod adding something will not be called.
@Mixin(value = LivingEntity.class, priority = QuiltStatusEffectInternals.MIXIN_PRIORITY)
public abstract class LivingEntityMixin extends Entity implements QuiltLivingEntityStatusEffectExtensions {
	@SuppressWarnings("ConstantConditions")
	public LivingEntityMixin() {
		super(null, null);
	}

	@Shadow
	@Final
	private Map<Holder<StatusEffect>, StatusEffectInstance> activeStatusEffects;

	@Shadow
	protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

	@Unique
	private StatusEffectRemovalReason quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean removeStatusEffect(@NotNull Holder<StatusEffect> type, @NotNull StatusEffectRemovalReason reason) {
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
					target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied(Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
					shift = At.Shift.AFTER
			)
	)
	private void quilt$callOnAppliedEvent(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, false);
	}

	@WrapOperation(
			method = "onStatusEffectRemoved",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V"
			)
	)
	private void quilt$callOnRemovedWithReason(StatusEffect instance, AttributeContainer attributes, Operation<Void> original, StatusEffectInstance effect) {
		instance.onRemoved((LivingEntity) (Object) this, attributes, effect, this.quilt$lastRemovalReason);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved((LivingEntity) (Object) this, effect, this.quilt$lastRemovalReason);
	}

	@Inject(
		method = "removeStatusEffect(Lnet/minecraft/registry/Holder;)Z",
		at = @At(
			value = "HEAD"
		),
		cancellable = true
	)
	public void quilt$shouldRemoveEffect(Holder<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		StatusEffectInstance instance = this.activeStatusEffects.get(effect);
		if (instance != null) {
			if (!StatusEffectUtils.shouldRemove((LivingEntity) (Object) this, instance, StatusEffectRemovalReason.GENERIC_ONE)) {
				cir.setReturnValue(false);
			}
		}
	}

	@WrapOperation(
			method = "removeStatusEffect(Lnet/minecraft/registry/Holder;)Z",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"
			)
	)
	public void quilt$addRemoveStatusEffectReason(LivingEntity instance, StatusEffectInstance effect, Operation<Void> original) {
		this.quilt$lastRemovalReason = StatusEffectRemovalReason.GENERIC_ONE;
		original.call(instance, effect);
		this.quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;
	}

	@WrapOperation(
			method = "clearStatusEffects",
			at = @At(
				value = "INVOKE",
				target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"
			)
	)
	private Iterator<StatusEffectInstance> quilt$filterStatusEffects(Collection<StatusEffectInstance> instance, Operation<Iterator<StatusEffectInstance>> original) {
		return Iterators.filter(original.call(instance), effect -> StatusEffectUtils.shouldRemove(
			(LivingEntity) (Object) this, effect, StatusEffectRemovalReason.GENERIC_ALL
		));
	}

	@WrapOperation(method = "tickStatusEffects", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V")
	)
	private void quilt$removeWithExpiredReason(LivingEntity instance, StatusEffectInstance effect, Operation<Void> original) {
		this.quilt$lastRemovalReason = StatusEffectRemovalReason.EXPIRED;
		original.call(instance, effect);
	}

	@WrapOperation(
		method = "onStatusEffectUpgraded",
		at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V"
		)
	)
	private void quilt$removeWithUpgradeApplyingReason(StatusEffect instance, AttributeContainer attributes, Operation<Void> original, StatusEffectInstance statusEffectInstance) {
		instance.onRemoved((LivingEntity) (Object) this, attributes, statusEffectInstance, StatusEffectRemovalReason.UPGRADE_REAPPLYING);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved((LivingEntity) (Object) this, statusEffectInstance, StatusEffectRemovalReason.UPGRADE_REAPPLYING);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(
			method = "onStatusEffectUpgraded",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied(Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
				shift = At.Shift.AFTER
			)
	)
	private void quilt$callOnAppliedEvent_upgradeReapplying(StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, true);
	}
}
