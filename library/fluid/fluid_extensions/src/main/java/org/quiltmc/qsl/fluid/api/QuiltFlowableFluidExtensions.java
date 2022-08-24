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
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.loot.LootTables;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.quiltmc.qsl.base.api.util.InjectedInterface;

import javax.annotation.Nullable;

@InjectedInterface({WaterFluid.class, LavaFluid.class})
public interface QuiltFlowableFluidExtensions {

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

	int LAVA_FOG_COLOR = 0x991900;

	Identifier WATER_FISHING_LOOT_TABLE = LootTables.FISHING_GAMEPLAY;

	int DROWNING_THRESHOLD = -20;

	/**
	 * The color of this fluid.
	 *
	 * @param state - FluidState of the fluid the color is requested from.
	 * @param world - World the wanted fluid is in (used for the Biome).
	 * @param pos   - Position where the fluid is located.
	 * @return - color as a int, based on in which biome it is in.
	 */
	default int getColor(FluidState state, World world, BlockPos pos) {
		return world.getBiome(pos).value().getWaterColor();
	}

	/**
	 * 0.8F is the default for water
	 * 0.5F is the default for lava
	 *
	 * @param state    - FluidState of the viscosity the color is requested from.
	 * @param affected - Entity which is in the fluid.
	 * @return - Viscosity of the fluidstate as a float.
	 * @see QuiltFlowableFluidExtensions#WATER_VISCOSITY
	 * @see QuiltFlowableFluidExtensions#LAVA_VISCOSITY
	 */
	default float getHorizontalViscosity(FluidState state, Entity affected) {
		if (state.isIn(FluidTags.LAVA)) return LAVA_VISCOSITY;
		return WATER_VISCOSITY;
	}

	/**
	 * Default for water and lava is 0.8F
	 *
	 * @param state    - FluidState of the fluid the viscosity is requested from.
	 * @param affected - Entity which is in the fluid.
	 * @return - Viscosity of the fluidstate as a float.
	 * @see QuiltFlowableFluidExtensions#WATER_VISCOSITY
	 */
	default float getVerticalViscosity(FluidState state, Entity affected) {
		if (state.isIn(FluidTags.LAVA)) return LAVA_VISCOSITY;
		return WATER_VISCOSITY;
	}

	/**
	 * 0.014F is the default for water
	 * 7 / 3000 is the default for lava in the overworld
	 * 0.007F is the default for lava in the nether
	 *
	 * @param state    - FluidState of the fluid the PushStrength is requested from.
	 * @param affected - Entity which is in the fluid.
	 * @return - PushStrength of the fluidstate.
	 * @see QuiltFlowableFluidExtensions#WATER_PUSH_STRENGTH
	 * @see QuiltFlowableFluidExtensions#LAVA_PUSH_STRENGTH_OVERWORLD
	 * @see QuiltFlowableFluidExtensions#LAVA_PUSH_STRENGTH_ULTRAWARM
	 */
	default float getPushStrength(FluidState state, Entity affected) {
		if (state.isIn(FluidTags.LAVA)) {
			return affected.world.getDimension().ultraWarm() ? LAVA_PUSH_STRENGTH_ULTRAWARM : LAVA_PUSH_STRENGTH_OVERWORLD;
		}
		return WATER_PUSH_STRENGTH;
	}

	/**
	 * Toggles whether a player can sprint swim in your fluid
	 *
	 * @param state    - Fluidstate of the fluid the boolean is requested from.
	 * @param affected - Entity which is in the fluid
	 * @return - A boolean, which tells if sprint swimming is allowed or not
	 */
	default boolean allowSprintSwimming(FluidState state, Entity affected) {
		if (state.isIn(FluidTags.LAVA)) {
			return false;
		}
		return true;
	}

	/**
	 * Modifies the horizontal viscosity, taking Dolphin's Grace into the mix.
	 *
	 * @param affected            - The LivingEntity whose horizontalViscosity should be modified.
	 * @param horizontalViscosity - Unmodified horizontalViscosity of the fluid.
	 * @return an updated horizontalViscosity, specifically regarding potion effects
	 */
	default float modifyEntityHorizontalViscosity(LivingEntity affected, float horizontalViscosity) {
		if (affected.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
			horizontalViscosity = 0.96f;
		}
		return horizontalViscosity;
	}

	/**
	 * Whether an Entity can swim when quickly double pressing the space bar inside a fluid.
	 *
	 * @param state    - The fluidstate of the fluid the entity wants to swim in.
	 * @param affected - The entity which is in the fluid.
	 * @return - A boolean representing whether the entity can double tap sprint swim.
	 */
	default boolean enableDoubleTapSpacebarSwimming(FluidState state, Entity affected) {
		return true;
	}

	/**
	 * Whether a fishing bobber floats on top of the fluid or not.
	 *
	 * @param state    - The fluidstate of the fluid the bobber wants to float on.
	 * @param affected - The FishingBobberEntity instance, which wants to float on the fluid.
	 * @return - A boolean representing whether the bobber can float on the fluid.
	 */
	default boolean bobberFloats(FluidState state, FishingBobberEntity affected) {
		return true;
	}

	/**
	 * Whether a fishing bobber can fish in a fluid or not.
	 *
	 * @param state    - The fluidstate of the fluid the bobber wants to fish in.
	 * @param affected - The FishingBobberEntity instance, which wants to fish in the fluid.
	 * @return - A boolean representing if the bobber can fish in the fluid.
	 */
	default boolean canFish(FluidState state, FishingBobberEntity affected) {
		return true;
	}

	/**
	 * Whether the fluid can extinguish burning entities.
	 *
	 * @param state    - The fluidstate of the fluid the entity is in.
	 * @param affected - The entity which is in the fluid.
	 * @return - A boolean representing whether the entity can be extinguished.
	 */
	default boolean canExtinguish(FluidState state, Entity affected) {
		return true;
	}

	/**
	 * Whether the fluid can ignite entities.
	 *
	 * @param state    - The fluidstate of the fluid the entity is in.
	 * @param affected - The entity which is in the fluid.
	 * @return - A boolean representing whether the entity can be ignited by the fluid.
	 */
	default boolean canIgnite(FluidState state, Entity affected) {
		return !canExtinguish(state, affected);
	}

	/**
	 * Returns the remaining air the entity has.
	 *
	 * @param air    - The air the entity currently has.
	 * @param entity - The entity which is currently drowning.
	 * @param random -  A RandomGenerator
	 * @return - The remaining air for the next tick
	 */
	default int getRemainingAir(int air, LivingEntity entity, RandomGenerator random) {
		int i = EnchantmentHelper.getRespiration(entity);
		return i > 0 && random.nextInt(i + 1) > 0 ? air : air - 1;
	}

	/**
	 * Density in kilograms per cubic meter
	 * 1000 is the default for water
	 * 3100 is the default for lava
	 *
	 * @param world    - The world the fluid is located in.
	 * @param blockpos - The position of the fluid.
	 * @return - The density of the fluid.
	 * @see QuiltFlowableFluidExtensions#WATER_DENSITY
	 * @see QuiltFlowableFluidExtensions#LAVA_DENSITY
	 */
	default float getDefaultDensity(World world, BlockPos blockpos) {
		if (world.getFluidState(blockpos).isIn(FluidTags.LAVA)) return LAVA_DENSITY;
		return WATER_DENSITY;
	}

	/**
	 * Temperature in Kelvin
	 * 300 is the default for water
	 * 1500 is the default for lava
	 *
	 * @param world    - The world the fluid is located in.
	 * @param blockpos - The position of the fluid.
	 * @return - The temperature of the fluid.
	 * @see QuiltFlowableFluidExtensions#WATER_TEMPERATURE
	 * @see QuiltFlowableFluidExtensions#LAVA_TEMPERATURE
	 */
	default float getDefaultTemperature(World world, BlockPos blockpos) {
		if (world.getFluidState(blockpos).isIn(FluidTags.LAVA)) return LAVA_TEMPERATURE;
		return WATER_TEMPERATURE;
	}

	/**
	 * 0 waters default equals complete fall damage reduction
	 * 0.5 lavas default equals half fall damage reduction
	 * 1 is no fall damage reduction whatsoever
	 *
	 * @param entity - The entity for which the fall damage reduction shall be calculated.
	 * @return - The fall damage reduction ranging from 1.0f to 0.0f.
	 * @see QuiltFlowableFluidExtensions#FULL_FALL_DAMAGE_REDUCTION
	 * @see QuiltFlowableFluidExtensions#HALF_FALL_DAMAGE_REDUCTION
	 * @see QuiltFlowableFluidExtensions#NO_FALL_DAMAGE_REDUCTION
	 */
	default float getFallDamageReduction(Entity entity) {
		BlockPos entityPos = entity.getBlockPos();

		if (entity.world.getFluidState(entityPos).isIn(FluidTags.LAVA)) return HALF_FALL_DAMAGE_REDUCTION;

		return FULL_FALL_DAMAGE_REDUCTION;
	}

	/**
	 * Water fog color is special cased to be -1. Any other
	 * value returned will be treated as a normal color.
	 *
	 * @param state    - State of the fluid the fog color is requested for.
	 * @param affected - The entity which is in the fluid.
	 * @return - The fog color as an int.
	 */
	default int getFogColor(FluidState state, Entity affected) {
		if (state.isIn(FluidTags.LAVA)) return LAVA_FOG_COLOR;
		return WATER_FOG_COLOR;
	}

	/**
	 * @param state        - State of the fluid the fog start distance is requested for.
	 * @param affected     - The entity which is in the fluid and looking through it.
	 * @param viewDistance - The view distance of the entity.
	 * @return - The distance for the fog to start.
	 * @see QuiltFlowableFluidExtensions#WATER_FOG_START
	 */
	default float getFogStart(FluidState state, Entity affected, float viewDistance) {
		return WATER_FOG_START;
	}

	/**
	 * Only reason default impl is complicated is because
	 * water has a fade-in effect. Feel free to disregard
	 * it and simply return a value.
	 *
	 * @param state        - State of the fluid the fog end distance is requested for.
	 * @param affected     - The entity which is in the fluid and looking through it.
	 * @param viewDistance - The view distance of the entity.
	 * @return - The distance for the fog to end.
	 */
	default float getFogEnd(FluidState state, Entity affected, float viewDistance) {
		float distance = 192.0F;
		if (affected instanceof ClientPlayerEntity player) {
			distance *= Math.max(0.25F, player.getUnderwaterVisibility());
		}
		return distance * 0.5f;
	}

	/**
	 * Gets the splash sound that shall be played when falling into the fluid.
	 *
	 * @param splashing - The entity which splashed into the fluid.
	 * @param splashPos - The position where the entity fell into the fluid.
	 * @param random    - A random generator.
	 * @return - The SoundEvent which shall be played when falling into it.
	 */

	@Nullable
	default SoundEvent getSplashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return SoundEvents.ENTITY_PLAYER_SPLASH;
	}

	/**
	 * This method is used, when the sound emitted from falling into the fluid is greated than 0.25f.
	 *
	 * @param splashing - The entity which splashed into the fluid.
	 * @param splashPos - The position where the entity fell into the fluid.
	 * @param random    - A random generator.
	 * @return - The SoundEvent which shall be played when fast falling into it.
	 * @see QuiltFlowableFluidExtensions#onSplash
	 */
	@Nullable
	default SoundEvent getHighSpeedSplashSound(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
	}

	/**
	 * Gets the splashing particle effect.
	 *
	 * @param splashing - The entity which fell into the fluid.
	 * @param splashPos - The position where the entity fell into the fluid.
	 * @param random    - A random generator.
	 * @return - A ParticleEffect which shall be played when falling into it.
	 */
	@Nullable
	default ParticleEffect getSplashParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return ParticleTypes.ASH;
	}

	/**
	 * Gets the bubble particle effect.
	 *
	 * @param splashing - The entity which fell into the fluid.
	 * @param splashPos - The position where the entity fell into the fluid.
	 * @param random    - A random generator.
	 * @return - A ParticleEffect which shall be played when bubbles form.
	 */
	@Nullable
	default ParticleEffect getBubbleParticle(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return ParticleTypes.BUBBLE;
	}

	/**
	 * Gets the splash game event.
	 *
	 * @param splashing - The entity which fell into the fluid.
	 * @param splashPos - The position where the entity fell into the fluid.
	 * @param random    - A random generator.
	 * @return - A GameEvent when the entity falls into the fluid.
	 */
	@Nullable
	default GameEvent getSplashGameEvent(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		return GameEvent.SPLASH;
	}

	/**
	 * Gets the loottable of the fluid.
	 *
	 * @return - The Identifier for the loottable of the fluid,
	 */
	default Identifier getFishingLootTable() {
		return WATER_FISHING_LOOT_TABLE;
	}

	/**
	 * Provides a way to determine if a boat can swim on a specific fluid or not.
	 *
	 * @return - A boolean representing whether a boat can float on top of this fluid.
	 */
	default boolean canBoatSwimOn() {
		return true;
	}

	// Overriding of any methods below this comment is generally unnecessary,
	// and only made available to cover as many cases as possible. Thus no javadoc is provided.

	default void spawnSplashParticles(Entity splashing, Vec3d splashPos, RandomGenerator random) {
		for (int i = 0; i < 1.0f + splashing.getDimensions(splashing.getPose()).width * 20.0f; ++i) {
			double xOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			double zOffset = (random.nextDouble() * 2.0 - 1.0) * (double) splashing.getDimensions(splashing.getPose()).width;
			int yFloor = MathHelper.floor(splashing.getY());
			ParticleEffect particle = getSplashParticle(splashing, splashPos, random);
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
			ParticleEffect particle = getBubbleParticle(splashing, splashPos, random);
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

		SoundEvent sound = volume < 0.25f ? getSplashSound(splashing, pos, random) : getHighSpeedSplashSound(splashing, pos, random);
		if (sound != null) {
			splashing.playSound(
					sound,
					(float) volume,
					1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f
			);
		}

		spawnBubbleParticles(splashing, pos, random);
		spawnSplashParticles(splashing, pos, random);

		GameEvent splash = getSplashGameEvent(splashing, pos, random);
		if (splash != null) {
			splashing.emitGameEvent(splash);
		}
	}

	/**
	 * Here you can modify viscosity and speed based on enchantments.
	 *
	 * @return a Helperclass which contains the calculated horizontalViscosity and speed. The class contains two fields, which are both floats.
	 */
	default FluidEnchantmentHelper customEnchantmentEffects(Vec3d movementInput, LivingEntity entity, float horizontalViscosity, float speed) {
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
		return new FluidEnchantmentHelper(horizontalViscosity, speed);
	}

	default void doDrownEffects(FluidState state, LivingEntity drowning, RandomGenerator random) {
		boolean isPlayer = drowning instanceof PlayerEntity;
		boolean invincible = isPlayer && ((PlayerEntity) drowning).getAbilities().invulnerable;
		if (!drowning.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(drowning) && !invincible) {
			drowning.setAir(getRemainingAir(drowning.getAir(), drowning, random));
			if (drowning.getAir() == DROWNING_THRESHOLD) {
				drowning.setAir(0);
				Vec3d vec3d = drowning.getVelocity();

				for (int i = 0; i < 8; ++i) {
					double f = random.nextDouble() - random.nextDouble();
					double g = random.nextDouble() - random.nextDouble();
					double h = random.nextDouble() - random.nextDouble();
					ParticleEffect particle = getBubbleParticle(drowning, drowning.getPos(), random);
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
