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

package org.quiltmc.qsl.fluid.api;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;

import javax.annotation.Nullable;

public interface FlowableFluidExtensions {

	float WATER_VISCOSITY = 0.8f;
	float LAVA_VISCOSITY = 0.5f;
	float WATER_PUSH_STRENGTH = 0.014f;
	float LAVA_PUSH_STRENGTH_OVERWORLD = 7 / 3000f;
	float LAVA_PUSH_STRENGTH_ULTRAWARM = 0.007f;
	float WATER_DENSITY = 1000f;
	float LAVA_DENSITY = 3100f;
	float WATER_TEMPERATURE = 300f;
	float LAVA_TEMPERATURE = 1500f;
	float WATER_FOG_START = -8.0f;
	float FULL_FALL_DAMAGE_REDUCTION = 0f;
	float HALF_FALL_DAMAGE_REDUCTION = 0.5f;
	float NO_FALL_DAMAGE_REDUCTION = 0f;
	int WATER_FOG_COLOR = -1;

	/**
	 * The color of this fluid.
	 */
	default int getColor(FluidState state, World world, BlockPos pos) {
		return world.getBiome(pos).value().getWaterColor();
	}

	/**
	 * 0.8F is the default for water
	 * 0.5F is the default for lava
	 * @see FlowableFluidExtensions#WATER_VISCOSITY
	 * @see FlowableFluidExtensions#LAVA_VISCOSITY
	 */
	default float getHorizontalViscosity(FluidState state, Entity affected) {
		return WATER_VISCOSITY;
	}

	/**
	 * Default for water and lava is 0.8F
	 * @see FlowableFluidExtensions#WATER_VISCOSITY
	 */
	default float getVerticalViscosity(FluidState state, Entity affected) {
		return WATER_VISCOSITY;
	}

	/**
	 * 0.014F is the default for water
	 * 7 / 3000 is the default for lava in the overworld
	 * 0.007F is the default for lava in the nether
	 * @see FlowableFluidExtensions#WATER_PUSH_STRENGTH
	 * @see FlowableFluidExtensions#LAVA_PUSH_STRENGTH_OVERWORLD
	 * @see FlowableFluidExtensions#LAVA_PUSH_STRENGTH_ULTRAWARM
	 */
	default float getPushStrength(FluidState state, Entity affected) {
		return WATER_PUSH_STRENGTH;
	}

	/**
	 * Toggles weather or not a player can sprint swim in your fluid
	 */
	default boolean canSprintSwim(FluidState state, Entity affected) {
		return true;
	}

	/**
	 * @return an updated horizontalViscosity, specifically regarding potion effects
	 */
	default float modifyHorizontalViscosity(LivingEntity affected, float horizontalViscosity) {
		if (affected.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
			horizontalViscosity = 0.96f;
		}
		return horizontalViscosity;
	}

	default boolean enableSpacebarSwimming(FluidState state, Entity affected) {
		return true;
	}

	default boolean bobberFloats(FluidState state, Entity affected) {
		return true;
	}

	default boolean canExtinguish(FluidState state, Entity affected) {
		return true;
	}

	default boolean canIgnite(FluidState state, Entity affected) {
		return !canExtinguish(state, affected);
	}

	default int getNextAirSubmerged(int air, LivingEntity entity, RandomGenerator random) {
		int i = EnchantmentHelper.getRespiration(entity);
		return i > 0 && random.nextInt(i + 1) > 0 ? air : air - 1;
	}

	/**
	 * Density in kilograms per cubic meter
	 * 1000 is the default for water
	 * 3100 is the default for lava
	 * @see FlowableFluidExtensions#WATER_DENSITY
	 * @see FlowableFluidExtensions#LAVA_DENSITY
	 */
	default float defaultDensity(World world, BlockPos blockpos) {
		return WATER_DENSITY;
	}

	/**
	 * Temperature in Kelvin
	 * 300 is the default for water
	 * 1500 is the default for lava
	 * @see FlowableFluidExtensions#WATER_TEMPERATURE
	 * @see FlowableFluidExtensions#LAVA_TEMPERATURE
	 *
	 */
	default float defaultTemperature(World world, BlockPos blockpos) {
		return WATER_TEMPERATURE;
	}

	/**
	 * 0 waters default equals complete fall damage reduction
	 * 0.5 lavas default equals half fall damage reduction
	 * 1 is no fall damage reduction whatsoever
	 * @see FlowableFluidExtensions#FULL_FALL_DAMAGE_REDUCTION
	 * @see FlowableFluidExtensions#HALF_FALL_DAMAGE_REDUCTION
	 * @see FlowableFluidExtensions#NO_FALL_DAMAGE_REDUCTION
	 */
	default float fallDamageReduction(Entity entity) {
		return FULL_FALL_DAMAGE_REDUCTION;
	}

	/**
	 * Water fog color is special cased to be -1. Any other
	 * value returned will be treated as a normal color.
	 */
	default int getFogColor(FluidState state, Entity affected) {
		return WATER_FOG_COLOR;
	}

	/**
	 * @see FlowableFluidExtensions#WATER_FOG_START
	 */
	default float getFogStart(FluidState state, Entity affected, float viewDistance) {
		return WATER_FOG_START;
	}

	/**
	 * Only reason default impl is complicated is because
	 * water has a fade-in effect. Feel free to disregard
	 * it and simply return a value.
	 */
	default float getFogEnd(FluidState state, Entity affected, float viewDistance) {
		float distance = 192.0F;
		if (affected instanceof ClientPlayerEntity player) {
			distance *= Math.max(0.25F, player.getUnderwaterVisibility());
			/*Biome biome = player.world.getBiome(player.getBlockPos());
			if (biome.getCategory() == Biome.Category.SWAMP) {
				distance *= 0.85F;
			}*/
		}
		return distance * 0.5f;
	}

	@Nullable
	default SoundEvent splashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return SoundEvents.ENTITY_PLAYER_SPLASH;
	}

	@Nullable
	default SoundEvent highSpeedSplashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
	}

	@Nullable
	default ParticleEffect splashParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return ParticleTypes.ASH;
	}

	@Nullable
	default ParticleEffect bubbleParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return ParticleTypes.BUBBLE;
	}

	@Nullable
	default GameEvent splashGameEvent(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return GameEvent.SPLASH;
	}

	// Overriding of any methods below this comment is generally unnecessary,
	// and only made available to cover as many cases as possible.

	default void spawnSplashParticles(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		for (int i = 0; i < 1.0f + splashing.getDimensions(splashing.getPose()).width * 20.0f; ++i) {
			double xOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			double zOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			int yFloor = MathHelper.floor(splashing.getY());
			ParticleEffect particle = splashParticle(splashing, splashPos, random);
			if (particle != null) {
				splashing.world.addParticle(
						particle,
						splashing.getX() + xOffset,
						yFloor + 1.0f,
						splashing.getZ() + zOffset,
						splashPos.x,
						splashPos.y,
						splashPos.z
				);
			}
		}
	}

	default void spawnBubbleParticles(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		for (int i = 0; i < 1.0f + splashing.getDimensions(splashing.getPose()).width * 20.0f; ++i) {
			double xOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			double zOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			int yFloor = MathHelper.floor(splashing.getY());
			ParticleEffect particle = bubbleParticle(splashing, splashPos, random);
			if (particle != null) {
				splashing.world.addParticle(
						particle,
						splashing.getX() + xOffset,
						yFloor + 1.0f,
						splashing.getZ() + zOffset,
						splashPos.x,
						splashPos.y - random.nextDouble() * 0.2,
						splashPos.z
				);
			}

		}
	}

	default void onSplash(World world, Vec3d pos, Entity splashing, RandomGenerator random) {
		Entity passenger = splashing.hasPassengers() && splashing.getPrimaryPassenger() != null ? splashing.getPrimaryPassenger() : splashing;
		float volumeMultiplier = passenger == splashing ? 0.2f : 0.9f;
		Vec3d velocity = passenger.getVelocity();
		double volume = Math.min(
				1.0f,
				Math.sqrt(velocity.x * velocity.x * 0.2 + velocity.y * velocity.y + velocity.z * velocity.z * 0.2D) * volumeMultiplier
		);

		SoundEvent sound = volume < 0.25f ? splashSound(splashing, pos, random) : highSpeedSplashSound(splashing, pos, random);
		if (sound != null) {
			splashing.playSound(
					sound,
					(float) volume,
					1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f
			);
		}

		spawnBubbleParticles(splashing, pos, random);
		spawnSplashParticles(splashing, pos, random);

		GameEvent splash = splashGameEvent(splashing, pos, random);
		if (splash != null) {
			splashing.emitGameEvent(splash);
		}
	}

	/**
	 * Here you can modify viscosity and speed based on enchantments.
	 * @return a float array where [0] holds updated horizontal viscosity, and [1] holds updated speed.
	 */
	default float[] customEnchantmentEffects(Vec3d movementInput, LivingEntity entity, float horizontalViscosity, float speed) {
		float[] values = new float[2];

		float depthStriderLevel = EnchantmentHelper.getDepthStrider(entity);
		if (depthStriderLevel > 3.0f) {
			depthStriderLevel = 3.0f;
		}

		if (!entity.isOnGround()) {
			depthStriderLevel *= 0.5f;
		}

		if (depthStriderLevel > 0.0f) {
			horizontalViscosity += (0.546f - horizontalViscosity) * depthStriderLevel / 3.0f;
			speed += (entity.getMovementSpeed() - speed) * depthStriderLevel / 3.0f;
		}

		values[0] = horizontalViscosity;
		values[1] = speed;

		return values;
	}

	default void drownEffects(FluidState state, LivingEntity drowning, RandomGenerator random) {
		boolean isPlayer = drowning instanceof PlayerEntity;
		boolean invincible = isPlayer && ((PlayerEntity) drowning).getAbilities().invulnerable;
		if (!drowning.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(drowning) && !invincible) {
			drowning.setAir(getNextAirSubmerged(drowning.getAir(), drowning, random));
			// if out of air
			if (drowning.getAir() == -20) {
				drowning.setAir(0);
				Vec3d vec3d = drowning.getVelocity();

				for (int i = 0; i < 8; ++i) {
					double f = random.nextDouble() - random.nextDouble();
					double g = random.nextDouble() - random.nextDouble();
					double h = random.nextDouble() - random.nextDouble();
					ParticleEffect particle = bubbleParticle(drowning, drowning.getPos(), random);
					if (particle != null) {
						drowning.world.addParticle(
								particle,
								drowning.getX() + f,
								drowning.getY() + g,
								drowning.getZ() + h,
								vec3d.x, vec3d.y, vec3d.z);
					}
				}

				drowning.damage(DamageSource.DROWN, 2.0F);
			}
		}

		// dismount vehicles that can't swim (horses)
		if (!drowning.world.isClient && drowning.hasVehicle() && drowning.getVehicle() != null && !drowning.getVehicle().canBeRiddenInWater()) {
			drowning.stopRiding();
		}
	}

}
