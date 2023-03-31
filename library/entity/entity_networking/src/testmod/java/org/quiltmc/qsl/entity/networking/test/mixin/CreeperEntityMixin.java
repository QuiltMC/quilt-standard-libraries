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

package org.quiltmc.qsl.entity.networking.test.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.networking.test.TrackedDataTestInitializer;

/**
 * Don't add extra tracked data to existing mobs in actual mods!
 */
@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity {
	@SuppressWarnings("WrongEntityDataParameterClass")
	private static final TrackedData<ParticleEffect> PARTICLE = DataTracker.registerData(CreeperEntity.class, TrackedDataTestInitializer.PARTICLE_DATA_HANDLER);

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void quiltTestMod$addCustomTracker(CallbackInfo ci) {
		this.dataTracker.startTracking(PARTICLE, ParticleTypes.ANGRY_VILLAGER);
	}

	@Inject(method = "interactMob", at = @At("HEAD"))
	private void quiltTestMod$addCustomTracker(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (this.world.isClient) {
			this.world.addParticle(this.dataTracker.get(PARTICLE), this.getX(), this.getY() + 2, this.getZ(), 0, 0, 0);
		} else {
			if (player.getStackInHand(hand).getItem() == Items.STICK) {
				this.dataTracker.method_12778(PARTICLE, ParticleTypes.CRIT);
			} else if (player.getStackInHand(hand).getItem() == Items.TNT) {
				this.dataTracker.method_12778(PARTICLE, ParticleTypes.SMOKE);
			}
		}
	}
}
