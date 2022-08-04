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

package org.quiltmc.qsl.fluid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.loot.LootTables;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.quiltmc.qsl.fluid.api.FlowableFluidExtensions;
import org.quiltmc.qsl.fluid.api.FluidEnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin extends FlowableFluid implements FlowableFluidExtensions {

	@Override
	public float getDefaultTemperature(World world, BlockPos blockPos) {
		return LAVA_TEMPERATURE;
	}

	@Override
	public float getDefaultDensity(World world, BlockPos blockPos) {
		return LAVA_DENSITY;
	}

	@Override
	public float getHorizontalViscosity(FluidState state, Entity effected) {
		return LAVA_VISCOSITY;
	}

	@Override
	public float getVerticalViscosity(FluidState state, Entity effected) {
		return WATER_VISCOSITY;
	}

	@Override
	public float getPushStrength(FluidState state, Entity effected) {
		return effected.world.getDimension().ultraWarm() ? LAVA_PUSH_STRENGTH_ULTRAWARM : LAVA_PUSH_STRENGTH_OVERWORLD;
	}

	@Override
	public float getFallDamageReduction(Entity entity) {
		return HALF_FALL_DAMAGE_REDUCTION;
	}

	@Override
	public float getFogStart(FluidState state, Entity affected, float viewDistance) {
		if (affected.isSpectator()) {
			return -8.0f;
		} else if (affected instanceof LivingEntity living && living.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			return 0.0f;
		} else {
			return 0.25f;
		}
	}

	@Override
	public float getFogEnd(FluidState state, Entity affected, float viewDistance) {
		if (affected.isSpectator()) {
			return viewDistance / 2;
		} else if (affected instanceof LivingEntity living && living.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			return 3.0f;
		} else {
			return 1.0f;
		}
	}

	@Override
	public boolean canExtinguish(FluidState state, Entity effected) {
		return false;
	}

	@Override
	public int getColor(FluidState state, World world, BlockPos pos) {
		return LAVA_FOG_COLOR;
	}

	@Override
	public boolean allowSprintSwimming(FluidState state, Entity affected) {
		return false;
	}

	@Override
	public float modifyHorizontalViscosity(LivingEntity affected, float horizontalViscosity) {
		return horizontalViscosity;
	}

	@Override
	public boolean enableDoubleTapSpacebarSwimming(FluidState state, Entity affected) {
		return true;
	}

	@Override
	public void doDrownEffects(FluidState state, LivingEntity drowning, RandomGenerator random) {
	}

	@Override
	public int getFogColor(FluidState state, Entity affected) {
		return LAVA_FOG_COLOR;
	}

	@Override
	public SoundEvent getSplashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return null;
	}

	@Override
	public SoundEvent getHighSpeedSplashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return null;
	}

	@Override
	public ParticleEffect getSplashParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return null;
	}

	@Override
	public ParticleEffect getBubbleParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return null;
	}

	@Override
	public GameEvent getSplashGameEvent(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return null;
	}

	@Override
	public void spawnSplashParticles(Entity splashing, Vec3d splashPos, RandomGenerator random) {
	}

	@Override
	public void spawnBubbleParticles(Entity splashing, Vec3d splashPos, RandomGenerator random) {
	}

	@Override
	public void onSplash(World world, Vec3d pos, Entity splashing, RandomGenerator random) {
	}

	@Override
	public FluidEnchantmentHelper customEnchantmentEffects(Vec3d movementInput, LivingEntity entity, float horizontalViscosity, float speed) {
		return new FluidEnchantmentHelper(horizontalViscosity, speed);
	}

	@Override
	public Identifier getFishingLootTable() {
		return LootTables.EMPTY;
	}
}
