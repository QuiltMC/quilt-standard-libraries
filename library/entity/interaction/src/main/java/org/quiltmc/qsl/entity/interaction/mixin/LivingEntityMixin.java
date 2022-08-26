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

package org.quiltmc.qsl.entity.interaction.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.quiltmc.qsl.entity.interaction.api.LivingEntityAttackEvents;
import org.quiltmc.qsl.entity.interaction.impl.DamageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Unique
	private boolean quilt$damageCancelled = false;

	@ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float onTakeDamageModify(float damage, DamageSource source) {
		if (source.getAttacker() instanceof LivingEntity attacker) {
			DamageContext context = new DamageContext(attacker, attacker.getMainHandStack(), (LivingEntity)(Object)this, source, damage);
			LivingEntityAttackEvents.BEFORE.invoker().beforeDamage(context);
			quilt$damageCancelled = context.isCanceled();
			return context.getDamage();
		}
		return damage;
	}

	@Inject(method = "damage", at = @At(value = "HEAD", shift = At.Shift.AFTER), cancellable = true)
	private void onTakeDamageCancel(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getAttacker() instanceof LivingEntity && quilt$damageCancelled) {
			quilt$damageCancelled = false;
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "damage", at = @At("TAIL"))
	private void afterTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getAttacker() instanceof LivingEntity attacker) {
			LivingEntityAttackEvents.AFTER.invoker().afterDamage(attacker, attacker.getMainHandStack(), (LivingEntity)(Object)this, source, amount);
		}
	}
}
