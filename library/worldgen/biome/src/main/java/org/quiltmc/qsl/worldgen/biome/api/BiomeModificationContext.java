/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.api;

import java.util.Optional;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Allows {@link Biome} properties to be modified.
 */
public interface BiomeModificationContext {
	/**
	 * {@return the modification context for the biomes weather properties}
	 */
	WeatherContext getWeather();

	/**
	 * {@return the modification context for the biomes effects}
	 */
	EffectsContext getEffects();

	/**
	 * {@return the modification context for the biomes generation settings}
	 */
	GenerationSettingsContext getGenerationSettings();

	/**
	 * {@return the modification context for the biomes spawn settings}
	 */
	SpawnSettingsContext getSpawnSettings();

	interface WeatherContext {
		/**
		 * @see Biome#hasPrecipitation()
		 * @see Biome.Builder#hasPrecipitation(boolean)
		 */
		void setHasPrecipitation(boolean hasPrecipitation);

		/**
		 * @see Biome#getTemperature()
		 * @see Biome.Builder#temperature(float)
		 */
		void setTemperature(float temperature);

		/**
		 * @see Biome.Builder#temperatureModifier(Biome.TemperatureModifier)
		 */
		void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier);

		/**
		 * @see Biome.Weather#downfall()
		 * @see Biome.Builder#downfall(float)
		 */
		void setDownfall(float downfall);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	interface EffectsContext {
		/**
		 * @see BiomeEffects#getFogColor()
		 * @see BiomeEffects.Builder#fogColor(int)
		 */
		void setFogColor(int color);

		/**
		 * @see BiomeEffects#getWaterColor()
		 * @see BiomeEffects.Builder#waterColor(int)
		 */
		void setWaterColor(int color);

		/**
		 * @see BiomeEffects#getWaterFogColor()
		 * @see BiomeEffects.Builder#waterFogColor(int)
		 */
		void setWaterFogColor(int color);

		/**
		 * @see BiomeEffects#getSkyColor()
		 * @see BiomeEffects.Builder#skyColor(int)
		 */
		void setSkyColor(int color);

		/**
		 * @see BiomeEffects#getFoliageColor()
		 * @see BiomeEffects.Builder#foliageColor(int)
		 */
		void setFoliageColor(Optional<Integer> color);

		/**
		 * @see BiomeEffects#getFoliageColor()
		 * @see BiomeEffects.Builder#foliageColor(int)
		 */
		default void setFoliageColor(int color) {
			this.setFoliageColor(Optional.of(color));
		}

		/**
		 * @see BiomeEffects#getFoliageColor()
		 * @see BiomeEffects.Builder#foliageColor(int)
		 */
		default void clearFoliageColor() {
			this.setFoliageColor(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getGrassColor()
		 * @see BiomeEffects.Builder#grassColor(int)
		 */
		void setGrassColor(Optional<Integer> color);

		/**
		 * @see BiomeEffects#getGrassColor()
		 * @see BiomeEffects.Builder#grassColor(int)
		 */
		default void setGrassColor(int color) {
			this.setGrassColor(Optional.of(color));
		}

		/**
		 * @see BiomeEffects#getGrassColor()
		 * @see BiomeEffects.Builder#grassColor(int)
		 */
		default void clearGrassColor() {
			this.setGrassColor(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getGrassColorModifier()
		 * @see BiomeEffects.Builder#grassColorModifier(BiomeEffects.GrassColorModifier)
		 */
		void setGrassColorModifier(@NotNull BiomeEffects.GrassColorModifier colorModifier);

		/**
		 * @see BiomeEffects#getParticleConfig()
		 * @see BiomeEffects.Builder#particleConfig(BiomeParticleConfig)
		 */
		void setParticleConfig(Optional<BiomeParticleConfig> particleConfig);

		/**
		 * @see BiomeEffects#getParticleConfig()
		 * @see BiomeEffects.Builder#particleConfig(BiomeParticleConfig)
		 */
		default void setParticleConfig(@NotNull BiomeParticleConfig particleConfig) {
			this.setParticleConfig(Optional.of(particleConfig));
		}

		/**
		 * @see BiomeEffects#getParticleConfig()
		 * @see BiomeEffects.Builder#particleConfig(BiomeParticleConfig)
		 */
		default void clearParticleConfig() {
			this.setParticleConfig(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getLoopSound()
		 * @see BiomeEffects.Builder#loopSound(Holder)
		 */
		void setAmbientSound(Optional<Holder<SoundEvent>> sound);

		/**
		 * @see BiomeEffects#getLoopSound()
		 * @see BiomeEffects.Builder#loopSound(Holder)
		 */
		default void setAmbientSound(@NotNull Holder<SoundEvent> sound) {
			this.setAmbientSound(Optional.of(sound));
		}

		/**
		 * @see BiomeEffects#getLoopSound()
		 * @see BiomeEffects.Builder#loopSound(Holder)
		 */
		default void clearAmbientSound() {
			this.setAmbientSound(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getMoodSound()
		 * @see BiomeEffects.Builder#moodSound(BiomeMoodSound)
		 */
		void setMoodSound(Optional<BiomeMoodSound> sound);

		/**
		 * @see BiomeEffects#getMoodSound()
		 * @see BiomeEffects.Builder#moodSound(BiomeMoodSound)
		 */
		default void setMoodSound(@NotNull BiomeMoodSound sound) {
			this.setMoodSound(Optional.of(sound));
		}

		/**
		 * @see BiomeEffects#getMoodSound()
		 * @see BiomeEffects.Builder#moodSound(BiomeMoodSound)
		 */
		default void clearMoodSound() {
			this.setMoodSound(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getAdditionsSound()
		 * @see BiomeEffects.Builder#additionsSound(BiomeAdditionsSound)
		 */
		void setAdditionsSound(Optional<BiomeAdditionsSound> sound);

		/**
		 * @see BiomeEffects#getAdditionsSound()
		 * @see BiomeEffects.Builder#additionsSound(BiomeAdditionsSound)
		 */
		default void setAdditionsSound(@NotNull BiomeAdditionsSound sound) {
			this.setAdditionsSound(Optional.of(sound));
		}

		/**
		 * @see BiomeEffects#getAdditionsSound()
		 * @see BiomeEffects.Builder#additionsSound(BiomeAdditionsSound)
		 */
		default void clearAdditionsSound() {
			this.setAdditionsSound(Optional.empty());
		}

		/**
		 * @see BiomeEffects#getMusic()
		 * @see BiomeEffects.Builder#music(MusicSound)
		 */
		void setMusic(Optional<MusicSound> sound);

		/**
		 * @see BiomeEffects#getMusic()
		 * @see BiomeEffects.Builder#music(MusicSound)
		 */
		default void setMusic(@NotNull MusicSound sound) {
			this.setMusic(Optional.of(sound));
		}

		/**
		 * @see BiomeEffects#getMusic()
		 * @see BiomeEffects.Builder#music(MusicSound)
		 */
		default void clearMusic() {
			this.setMusic(Optional.empty());
		}
	}

	interface GenerationSettingsContext {
		/**
		 * Removes a feature from one of this biomes generation steps, and returns if any features were removed.
		 */
		boolean removeFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureKey);

		/**
		 * Removes a feature from all of this biomes generation steps, and returns if any features were removed.
		 */
		default boolean removeFeature(RegistryKey<PlacedFeature> placedFeatureKey) {
			boolean anyFound = false;

			for (GenerationStep.Feature step : GenerationStep.Feature.values()) {
				if (this.removeFeature(step, placedFeatureKey)) {
					anyFound = true;
				}
			}

			return anyFound;
		}

		/**
		 * Adds a feature to one of this biomes generation steps, identified by the placed feature's registry key.
		 *
		 * @see RegistryKeys#PLACED_FEATURE
		 */
		void addFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureKey);

		/**
		 * Adds a configured carver to one of this biomes generation steps.
		 */
		void addCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> carverKey);

		/**
		 * Removes all carvers with the given key from one of this biomes generation steps.
		 *
		 * @return True if any carvers were removed.
		 */
		boolean removeCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> configuredCarverKey);

		/**
		 * Removes all carvers with the given key from all of this biomes generation steps.
		 *
		 * @return {@code true} if any carvers were removed, or {@code false} otherwise
		 */
		default boolean removeCarver(RegistryKey<ConfiguredCarver<?>> configuredCarverKey) {
			boolean anyFound = false;

			for (GenerationStep.Carver step : GenerationStep.Carver.values()) {
				if (this.removeCarver(step, configuredCarverKey)) {
					anyFound = true;
				}
			}

			return anyFound;
		}
	}

	interface SpawnSettingsContext {
		/**
		 * Associated JSON property: {@code creature_spawn_probability}.
		 *
		 * @see SpawnSettings#getCreatureSpawnProbability()
		 * @see SpawnSettings.Builder#creatureSpawnProbability(float)
		 */
		void setCreatureSpawnProbability(float probability);

		/**
		 * Associated JSON property: {@code spawners}.
		 *
		 * @see SpawnSettings#getSpawnEntries(SpawnGroup)
		 * @see SpawnSettings.Builder#spawn(SpawnGroup, SpawnSettings.SpawnEntry)
		 */
		void addSpawn(SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry);

		/**
		 * Removes any spawns matching the given predicate from this biome, and returns true if any matched.
		 * <p>
		 * Associated JSON property: {@code spawners}.
		 */
		boolean removeSpawns(BiPredicate<SpawnGroup, SpawnSettings.SpawnEntry> predicate);

		/**
		 * Removes all spawns of the given entity type.
		 * <p>
		 * Associated JSON property: {@code spawners}.
		 *
		 * @return True if any spawns were removed.
		 */
		default boolean removeSpawnsOfEntityType(EntityType<?> entityType) {
			return this.removeSpawns((spawnGroup, spawnEntry) -> spawnEntry.type == entityType);
		}

		/**
		 * Removes all spawns of the given spawn group.
		 * <p>
		 * Associated JSON property: {@code spawners}.
		 */
		default void clearSpawns(SpawnGroup group) {
			this.removeSpawns((spawnGroup, spawnEntry) -> spawnGroup == group);
		}

		/**
		 * Removes all spawns.
		 * <p>
		 * Associated JSON property: {@code spawners}.
		 */
		default void clearSpawns() {
			this.removeSpawns((spawnGroup, spawnEntry) -> true);
		}

		/**
		 * Associated JSON property: {@code spawn_costs}.
		 *
		 * @see SpawnSettings#getSpawnDensity(EntityType)
		 * @see SpawnSettings.Builder#spawnCost(EntityType, double, double)
		 */
		void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit);

		/**
		 * Removes a spawn cost entry for a given entity type.
		 * <p>
		 * Associated JSON property: {@code spawn_costs}.
		 */
		void clearSpawnCost(EntityType<?> entityType);
	}
}
