/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.biome.impl.modification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Holder;
import net.minecraft.util.HolderSet;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;

@ApiStatus.Internal
public class BiomeModificationContextImpl implements BiomeModificationContext {
	private final DynamicRegistryManager registries;
	private final RegistryKey<Biome> biomeKey;
	private final Biome biome;
	private final WeatherContext weather;
	private final EffectsContext effects;
	private final GenerationSettingsContextImpl generationSettings;
	private final SpawnSettingsContextImpl spawnSettings;

	public BiomeModificationContextImpl(DynamicRegistryManager registries, RegistryKey<Biome> biomeKey, Biome biome) {
		this.registries = registries;
		this.biomeKey = biomeKey;
		this.biome = biome;
		this.weather = new WeatherContextImpl();
		this.effects = new EffectsContextImpl();
		this.generationSettings = new GenerationSettingsContextImpl();
		this.spawnSettings = new SpawnSettingsContextImpl();
	}

	@Override
	public WeatherContext getWeather() {
		return this.weather;
	}

	@Override
	public EffectsContext getEffects() {
		return this.effects;
	}

	@Override
	public GenerationSettingsContext getGenerationSettings() {
		return this.generationSettings;
	}

	@Override
	public SpawnSettingsContext getSpawnSettings() {
		return this.spawnSettings;
	}

	/**
	 * Re-freeze any immutable lists and perform general post-modification cleanup.
	 */
	void freeze() {
		this.generationSettings.freeze();
		this.spawnSettings.freeze();
	}

	private class WeatherContextImpl implements WeatherContext {
		private final Biome.Weather weather = biome.weather;

		@Override
		public void setPrecipitation(Biome.Precipitation precipitation) {
			this.weather.precipitation = Objects.requireNonNull(precipitation);
		}

		@Override
		public void setTemperature(float temperature) {
			this.weather.temperature = temperature;
		}

		@Override
		public void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
			this.weather.temperatureModifier = Objects.requireNonNull(temperatureModifier);
		}

		@Override
		public void setDownfall(float downfall) {
			this.weather.downfall = downfall;
		}
	}

	private class EffectsContextImpl implements EffectsContext {
		private final BiomeEffects effects = biome.getEffects();

		@Override
		public void setFogColor(int color) {
			this.effects.fogColor = color;
		}

		@Override
		public void setWaterColor(int color) {
			this.effects.waterColor = color;
		}

		@Override
		public void setWaterFogColor(int color) {
			this.effects.waterFogColor = color;
		}

		@Override
		public void setSkyColor(int color) {
			this.effects.skyColor = color;
		}

		@Override
		public void setFoliageColor(Optional<Integer> color) {
			this.effects.foliageColor = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColor(Optional<Integer> color) {
			this.effects.grassColor = Objects.requireNonNull(color);
		}

		@Override
		public void setGrassColorModifier(@NotNull BiomeEffects.GrassColorModifier colorModifier) {
			this.effects.grassColorModifier = Objects.requireNonNull(colorModifier);
		}

		@Override
		public void setParticleConfig(Optional<BiomeParticleConfig> particleConfig) {
			this.effects.particleConfig = Objects.requireNonNull(particleConfig);
		}

		@Override
		public void setAmbientSound(Optional<SoundEvent> sound) {
			this.effects.loopSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setMoodSound(Optional<BiomeMoodSound> sound) {
			this.effects.moodSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setAdditionsSound(Optional<BiomeAdditionsSound> sound) {
			this.effects.additionsSound = Objects.requireNonNull(sound);
		}

		@Override
		public void setMusic(Optional<MusicSound> sound) {
			this.effects.music = Objects.requireNonNull(sound);
		}
	}

	private class GenerationSettingsContextImpl implements GenerationSettingsContext {
		private final Registry<ConfiguredCarver<?>> carvers = registries.get(Registry.CONFIGURED_CARVER_KEY);
		private final Registry<PlacedFeature> features = registries.get(Registry.PLACED_FEATURE_KEY);
		private final GenerationSettings generationSettings = biome.getGenerationSettings();

		private boolean rebuildFlowerFeatures;

		/**
		 * Unfreeze the immutable lists found in the generation settings, and make sure they're filled up to every
		 * possible step if they're dense lists.
		 */
		GenerationSettingsContextImpl() {
			this.unfreezeCarvers();
			this.unfreezeFeatures();

			this.rebuildFlowerFeatures = false;
		}

		private void unfreezeCarvers() {
			var carversByStep = new EnumMap<GenerationStep.Carver, HolderSet<ConfiguredCarver<?>>>(GenerationStep.Carver.class);
			carversByStep.putAll(this.generationSettings.carvers);

			this.generationSettings.carvers = carversByStep;
		}

		private void unfreezeFeatures() {
			this.generationSettings.features = new ArrayList<>(this.generationSettings.features);
		}

		/**
		 * Re-freeze the lists in the generation settings to immutable variants, also fixes the flower features.
		 */
		public void freeze() {
			this.freezeCarvers();
			this.freezeFeatures();

			if (this.rebuildFlowerFeatures) {
				this.rebuildFlowerFeatures();
			}
		}

		private void freezeCarvers() {
			this.generationSettings.carvers = ImmutableMap.copyOf(this.generationSettings.carvers);
		}

		private void freezeFeatures() {
			this.generationSettings.features = ImmutableList.copyOf(this.generationSettings.features);
			// Replace the supplier to force a rebuild next time its called.
			this.generationSettings.allowedFeatures = Suppliers.memoize(() -> this.generationSettings.features.stream()
					.flatMap(HolderSet::stream)
					.map(Holder::value)
					.collect(Collectors.toSet()));
		}

		private void rebuildFlowerFeatures() {
			// Replace the supplier to force a rebuild next time its called.
			this.generationSettings.flowerFeatures = Suppliers.memoize(() -> this.generationSettings.features.stream()
					.flatMap(HolderSet::stream)
					.map(Holder::value)
					.flatMap(PlacedFeature::getDecoratedFeatures)
					.filter((configuredFeature) -> configuredFeature.getFeature() == Feature.FLOWER)
					.collect(ImmutableList.toImmutableList()));
		}

		@Override
		public boolean removeFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> placedFeatureKey) {
			PlacedFeature configuredFeature = this.features.getOrThrow(placedFeatureKey);

			int stepIndex = step.ordinal();
			List<HolderSet<PlacedFeature>> featureSteps = this.generationSettings.features;

			if (stepIndex >= featureSteps.size()) {
				return false; // The step was not populated with any features yet
			}

			HolderSet<PlacedFeature> featuresInStep = featureSteps.get(stepIndex);
			List<Holder<PlacedFeature>> features = new ArrayList<>(featuresInStep.stream().toList());

			if (features.removeIf(feature -> feature.value() == configuredFeature)) {
				featureSteps.set(stepIndex, HolderSet.createDirect(features));
				this.rebuildFlowerFeatures = true;

				return true;
			}

			return false;
		}

		@Override
		public void addFeature(GenerationStep.Feature step, RegistryKey<PlacedFeature> entry) {
			List<HolderSet<PlacedFeature>> featureSteps = this.generationSettings.features;
			int index = step.ordinal();

			// Add new empty lists for the generation steps that have no features yet
			while (index >= featureSteps.size()) {
				featureSteps.add(HolderSet.createDirect(Collections.emptyList()));
			}

			featureSteps.set(index, this.plus(featureSteps.get(index), this.features.getHolderOrThrow(entry)));

			// Ensure the list of flower features is up to date
			this.rebuildFlowerFeatures = true;
		}

		@Override
		public void addCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> entry) {
			// We do not need to delay evaluation of this since the registries are already fully built
			this.generationSettings.carvers.put(step, this.plus(this.generationSettings.carvers.get(step), carvers.getHolderOrThrow(entry)));
		}

		@Override
		public boolean removeCarver(GenerationStep.Carver step, RegistryKey<ConfiguredCarver<?>> configuredCarverKey) {
			ConfiguredCarver<?> carver = this.carvers.getOrThrow(configuredCarverKey);
			var genCarvers = new ArrayList<>(this.generationSettings.carvers.get(step).stream().toList());

			if (genCarvers.removeIf(entry -> entry.value() == carver)) {
				this.generationSettings.carvers.put(step, HolderSet.createDirect(genCarvers));
				return true;
			}

			return false;
		}

		private <T> HolderSet<T> plus(HolderSet<T> values, Holder<T> entry) {
			var list = new ArrayList<>(values.stream().toList());
			list.add(entry);
			return HolderSet.createDirect(list);
		}
	}

	private class SpawnSettingsContextImpl implements SpawnSettingsContext {
		private final SpawnSettings spawnSettings = biome.getSpawnSettings();
		private final EnumMap<SpawnGroup, List<SpawnSettings.SpawnEntry>> quiltSpawners = new EnumMap<>(SpawnGroup.class);

		SpawnSettingsContextImpl() {
			this.unfreezeSpawners();
			this.unfreezeSpawnCost();
		}

		private void unfreezeSpawners() {
			this.quiltSpawners.clear();

			for (SpawnGroup spawnGroup : SpawnGroup.values()) {
				Pool<SpawnSettings.SpawnEntry> entries = this.spawnSettings.spawners.get(spawnGroup);

				if (entries != null) {
					this.quiltSpawners.put(spawnGroup, new ArrayList<>(entries.getEntries()));
				} else {
					this.quiltSpawners.put(spawnGroup, new ArrayList<>());
				}
			}
		}

		private void unfreezeSpawnCost() {
			this.spawnSettings.spawnCosts = new HashMap<>(this.spawnSettings.spawnCosts);
		}

		public void freeze() {
			this.freezeSpawners();
			this.freezeSpawnCosts();
		}

		private void freezeSpawners() {
			Map<SpawnGroup, Pool<SpawnSettings.SpawnEntry>> spawners = new HashMap<>(this.spawnSettings.spawners);

			for (Map.Entry<SpawnGroup, List<SpawnSettings.SpawnEntry>> entry : this.quiltSpawners.entrySet()) {
				if (entry.getValue().isEmpty()) {
					spawners.put(entry.getKey(), Pool.empty());
				} else {
					spawners.put(entry.getKey(), Pool.of(entry.getValue()));
				}
			}

			this.spawnSettings.spawners = ImmutableMap.copyOf(spawners);
		}

		private void freezeSpawnCosts() {
			this.spawnSettings.spawnCosts = ImmutableMap.copyOf(this.spawnSettings.spawnCosts);
		}

		@Override
		public void setCreatureSpawnProbability(float probability) {
			this.spawnSettings.creatureSpawnProbability = probability;
		}

		@Override
		public void addSpawn(SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry) {
			Objects.requireNonNull(spawnGroup);
			Objects.requireNonNull(spawnEntry);

			this.quiltSpawners.get(spawnGroup).add(spawnEntry);
		}

		@Override
		public boolean removeSpawns(BiPredicate<SpawnGroup, SpawnSettings.SpawnEntry> predicate) {
			boolean anyRemoved = false;

			for (SpawnGroup group : SpawnGroup.values()) {
				if (this.quiltSpawners.get(group).removeIf(entry -> predicate.test(group, entry))) {
					anyRemoved = true;
				}
			}

			return anyRemoved;
		}

		@Override
		public void setSpawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
			Objects.requireNonNull(entityType);
			this.spawnSettings.spawnCosts.put(entityType, new SpawnSettings.SpawnDensity(gravityLimit, mass));
		}

		@Override
		public void clearSpawnCost(EntityType<?> entityType) {
			this.spawnSettings.spawnCosts.remove(entityType);
		}
	}
}
