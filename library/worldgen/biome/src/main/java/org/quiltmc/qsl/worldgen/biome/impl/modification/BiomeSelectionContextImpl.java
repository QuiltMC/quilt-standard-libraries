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

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.level.LevelProperties;

import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

@ApiStatus.Internal
public class BiomeSelectionContextImpl implements BiomeSelectionContext {
	private final DynamicRegistryManager dynamicRegistries;
	private final LevelProperties levelProperties;
	private final RegistryKey<Biome> key;
	private final Biome biome;
	private final Holder<Biome> entry;

	public BiomeSelectionContextImpl(DynamicRegistryManager dynamicRegistries, LevelProperties levelProperties, RegistryKey<Biome> key, Biome biome) {
		this.dynamicRegistries = dynamicRegistries;
		this.levelProperties = levelProperties;
		this.key = key;
		this.biome = biome;
		this.entry = dynamicRegistries.get(Registry.BIOME_KEY).getHolderOrThrow(this.key);
	}

	@Override
	public RegistryKey<Biome> getBiomeKey() {
		return this.key;
	}

	@Override
	public Biome getBiome() {
		return this.biome;
	}

	@Override
	public Holder<Biome> getBiomeRegistryEntry() {
		return this.entry;
	}

	@Override
	public Optional<RegistryKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> configuredFeature) {
		Registry<ConfiguredFeature<?, ?>> registry = this.dynamicRegistries.get(Registry.CONFIGURED_FEATURE_KEY);
		return registry.getKey(configuredFeature);
	}

	@Override
	public Optional<RegistryKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature placedFeature) {
		Registry<PlacedFeature> registry = this.dynamicRegistries.get(Registry.PLACED_FEATURE_KEY);
		return registry.getKey(placedFeature);
	}

	@Override
	public boolean validForStructure(RegistryKey<ConfiguredStructureFeature<?, ?>> key) {
		ConfiguredStructureFeature<?, ?> instance = this.dynamicRegistries.get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY).get(key);

		if (instance == null) {
			return false;
		}

		return instance.getBiomes().contains(getBiomeRegistryEntry());
	}

	@Override
	public Optional<RegistryKey<ConfiguredStructureFeature<?, ?>>> getStructureKey(ConfiguredStructureFeature<?, ?> configuredStructure) {
		Registry<ConfiguredStructureFeature<?, ?>> registry = this.dynamicRegistries.get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
		return registry.getKey(configuredStructure);
	}

	@Override
	public boolean canGenerateIn(RegistryKey<DimensionOptions> dimensionKey) {
		DimensionOptions dimension = this.levelProperties.getGeneratorOptions().getDimensions().get(dimensionKey);

		if (dimension == null) {
			return false;
		}

		return dimension.getChunkGenerator().getBiomeSource().getBiomes().stream().anyMatch(entry -> entry.value() == biome);
	}

	@Override
	public boolean isIn(TagKey<Biome> tag) {
		Registry<Biome> biomeRegistry = this.dynamicRegistries.get(Registry.BIOME_KEY);
		return biomeRegistry.getHolderOrThrow(this.getBiomeKey()).hasTag(tag);
	}

	@Override
	public <T> boolean doesRegistryEntryExist(RegistryKey<? extends Registry<? extends T>> registryKey, RegistryKey<T> entryKey) {
		return this.dynamicRegistries.getOptional(registryKey).map(registry -> registry.contains(entryKey)).orElse(false);
	}
}
