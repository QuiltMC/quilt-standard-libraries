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
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

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

// We want to make sure that our wrap operations are put before other mods,
// so that we wrap the vanilla call and not a mod's call.
// This is because we do not call the vanilla method, so any mod adding something will not be called.
@Mixin(value = LivingEntity.class, priority = QuiltStatusEffectInternals.MIXIN_PRIORITY)
abstract class LivingEntityMixin extends Entity implements QuiltLivingEntityStatusEffectExtensions {
	@SuppressWarnings("ConstantConditions")
	private LivingEntityMixin() {
		super(null, null);
		throw new AssertionError("dummy constructor called");
	}

	@Shadow
	@Final
	private Map<Holder<StatusEffect>, StatusEffectInstance> activeStatusEffects;

	@Shadow
	protected abstract void onEffectsRemoved(Collection<StatusEffectInstance> effects);

	@Unique
	private StatusEffectRemovalReason quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;

	// from injected interface
	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public boolean removeStatusEffect(@NotNull Holder<StatusEffect> type, @NotNull StatusEffectRemovalReason reason) {
		final StatusEffectInstance effect = this.activeStatusEffects.get(type);
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

	// from injected interface
	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public int clearStatusEffects(@NotNull StatusEffectRemovalReason reason) {
		if (this.getWorld().isClient) {
			return 0;
		}

		int removed = 0;
		final Iterator<StatusEffectInstance> it = this.activeStatusEffects.values().iterator();
		while (it.hasNext()) {
			final StatusEffectInstance effect = it.next();
			if (StatusEffectUtils.shouldRemove((LivingEntity) (Object) this, effect, reason)) {
				it.remove();
				this.onStatusEffectRemoved(effect, reason);
				removed++;
			}
		}

		return removed;
	}

	// from injected interface
	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public void onStatusEffectRemoved(@NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason) {
		this.quilt$lastRemovalReason = reason;
		this.onEffectsRemoved(List.of(effect));
		this.quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;
	}

	@Inject(
			method = "onStatusEffectApplied",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied(Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
					shift = At.Shift.AFTER
			)
	)
	private void callOnAppliedEvent(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, false);
	}

	// share effect instance rather than using @Local because it's less brittle
	@ModifyReceiver(
			method = "onEffectsRemoved",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getEffectType()"
					+ "Lnet/minecraft/registry/Holder;"
			)
	)
	private StatusEffectInstance shareEffectInstance(
			StatusEffectInstance instance,
			@Share("effectInstance") LocalRef<StatusEffectInstance> effectInstance
	) {
		effectInstance.set(instance);
		return instance;
	}

	@WrapOperation(
			method = "onEffectsRemoved",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved"
					+ "(Lnet/minecraft/entity/attribute/AttributeContainer;)V"
			)
	)
	private void callOnRemovedWithReason(
			StatusEffect instance, AttributeContainer attributes, Operation<Void> original,
			@Share("effectInstance") LocalRef<StatusEffectInstance> effectInstance
	) {
		final LivingEntity self = (LivingEntity) (Object) this;
		final StatusEffectInstance effect = effectInstance.get();
		instance.onRemoved(self, attributes, effect, this.quilt$lastRemovalReason);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved(self, effect, this.quilt$lastRemovalReason);
	}

	@Inject(
			method = "removeStatusEffect(Lnet/minecraft/registry/Holder;)Z",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	public void shouldRemoveEffect(Holder<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		final StatusEffectInstance instance = this.activeStatusEffects.get(effect);
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
				target = "Lnet/minecraft/entity/LivingEntity;onEffectsRemoved(Ljava/util/Collection;)V"
			)
	)
	public void addRemoveStatusEffectReason(
			LivingEntity instance, Collection<StatusEffectInstance> effects, Operation<Void> original
	) {
		this.quilt$lastRemovalReason = StatusEffectRemovalReason.GENERIC_ONE;
		original.call(instance, effects);
		this.quilt$lastRemovalReason = QuiltStatusEffectInternals.UNKNOWN_REASON;
	}

	@ModifyArg(
			method = "clearStatusEffects",
			at = @At(
				value = "INVOKE",
				target = "Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;"
			)
	)
	private Map<Holder<StatusEffect>, StatusEffectInstance> filterStatusEffects(
			Map<Holder<StatusEffect>, StatusEffectInstance> effects
	) {
		final Iterator<StatusEffectInstance> itr = effects.values().iterator();
		while (itr.hasNext()) {
			final StatusEffectInstance effect = itr.next();
			final boolean remove = StatusEffectUtils.shouldRemove(
					(LivingEntity) (Object) this, effect, StatusEffectRemovalReason.GENERIC_ALL
			);

			if (remove) {
				itr.remove();
			}
		}

		return effects;
	}

	@Redirect(
			method = "clearStatusEffects",
			at = @At(
				value = "INVOKE",
				target = "Ljava/util/Map;clear()V"
			)
	)
	private void stopClear(Map<?, ?> instance) {
		// don't clear map, effects are selectively removed in filterStatusEffects
	}

	@WrapOperation(method = "tickStatusEffects", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;onEffectsRemoved(Ljava/util/Collection;)V")
	)
	private void removeWithExpiredReason(
			LivingEntity instance, Collection<StatusEffectInstance> effects, Operation<Void> original
	) {
		this.quilt$lastRemovalReason = StatusEffectRemovalReason.EXPIRED;
		original.call(instance, effects);
	}

	@WrapOperation(
			method = "onStatusEffectUpgraded",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V"
			)
	)
	private void removeWithUpgradeApplyingReason(
			StatusEffect instance, AttributeContainer attributes, Operation<Void> original,
			StatusEffectInstance statusEffectInstance
	) {
		final LivingEntity self = (LivingEntity) (Object) this;
		instance.onRemoved(
				self,
				attributes,
				statusEffectInstance,
				StatusEffectRemovalReason.UPGRADE_REAPPLYING
		);
		StatusEffectEvents.ON_REMOVED.invoker().onRemoved(
				self,
				statusEffectInstance,
				StatusEffectRemovalReason.UPGRADE_REAPPLYING
		);
	}

	@Inject(
			method = "onStatusEffectUpgraded",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/effect/StatusEffect;onApplied"
					+ "(Lnet/minecraft/entity/attribute/AttributeContainer;I)V",
				shift = At.Shift.AFTER
			)
	)
	private void callOnAppliedEvent_upgradeReapplying(
			StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci
	) {
		StatusEffectEvents.ON_APPLIED.invoker().onApplied((LivingEntity) (Object) this, effect, true);
	}
}
