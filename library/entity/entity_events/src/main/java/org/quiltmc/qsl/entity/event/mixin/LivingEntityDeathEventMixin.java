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

package org.quiltmc.qsl.entity.event.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.event.api.LivingEntityDeathCallback;

@Mixin(value = { LivingEntity.class, ServerPlayerEntity.class })
public abstract class LivingEntityDeathEventMixin extends Entity {
	public LivingEntityDeathEventMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;update()V"))
	void quilt$invokeLivingEntityDeathEvent(DamageSource source, CallbackInfo ci) {
		if (!this.getWorld().isClient()) {
			LivingEntityDeathCallback.EVENT.invoker().onDeath((LivingEntity) (Object) this, source);
		}
	}
}
