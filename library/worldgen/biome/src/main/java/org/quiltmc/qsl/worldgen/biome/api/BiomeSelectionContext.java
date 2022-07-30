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

package org.quiltmc.qsl.worldgen.biome.api;

import java.util.List;
import java.util.Optional;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.HolderSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BuiltInRegistryKeys;

/**
 * Context given to a biome selector for deciding whether it applies to a biome or not.
 */
public interface BiomeSelectionContext {
	/**
	 * {@return the biome key}
	 */
	RegistryKey<Biome> getBiomeKey();

	/**
	 * {@return the biome with modifications by biome modifiers of higher priority already applied}
	 */
	Biome getBiome();

	/**
	 * {@return the biome holder}
	 */
	Holder<Biome> getBiomeHolder();

	/**
	 * {@return {@code true} if this biome has the given configured feature, which must be registered
	 * in the {@link net.minecraft.util.registry.BuiltinRegistries}, otherwise {@code false}}
	 * <p>
	 * This method is intended for use with the Vanilla configured features found in
	 * classes such as {@link net.minecraft.world.gen.feature.OreConfiguredFeatures}.
	 */
	default boolean hasBuiltInFeature(ConfiguredFeature<?, ?> configuredFeature) {
		RegistryKey<ConfiguredFeature<?, ?>> key = BuiltInRegistryKeys.get(configuredFeature);
		return this.hasFeature(key);
	}

	/**
	 * Returns true if this biome has the given placed feature, which must be registered
	 * in the {@link net.minecraft.util.registry.BuiltinRegistries}.
	 * <p>
	 * This method is intended for use with the Vanilla placed features found in
	 * classes such as {@link net.minecraft.world.gen.feature.OrePlacedFeatures}.
	 */
	default boolean hasBuiltInPlacedFeature(PlacedFeature placedFeature) {
		return this.hasPlacedFeature(BuiltInRegistryKeys.get(placedFeature));
	}

	/**
	 * {@return {@code true} if this biome contains a placed feature referencing a configured feature with the given key, otherwise {@code false}}
	 */
	default boolean hasFeature(RegistryKey<ConfiguredFeature<?, ?>> key) {
		List<HolderSet<PlacedFeature>> featureSteps = this.getBiome().getGenerationSettings().getFeatures();

		for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
			for (Holder<PlacedFeature> featureSupplier : featureSuppliers) {
				if (featureSupplier.value().getDecoratedFeatures().anyMatch(cf -> getFeatureKey(cf).orElse(null) == key)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * {@return {@code true} if this biome contains a placed feature with the given key, otherwise {@code false}}
	 */
	default boolean hasPlacedFeature(RegistryKey<PlacedFeature> key) {
		List<HolderSet<PlacedFeature>> featureSteps = this.getBiome().getGenerationSettings().getFeatures();

		for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
			for (Holder<PlacedFeature> featureSupplier : featureSuppliers) {
				if (this.getPlacedFeatureKey(featureSupplier.value()).orElse(null) == key) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Tries to retrieve the registry key for the given configured feature, which should be from this biomes
	 * current feature list. May be empty if the configured feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<RegistryKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> configuredFeature);

	/**
	 * Tries to retrieve the registry key for the given placed feature, which should be from this biomes
	 * current feature list. May be empty if the placed feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<RegistryKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature placedFeature);

	/**
	 * {@return {@code true} if the given built-in configured structure from {@link net.minecraft.util.registry.BuiltinRegistries}
	 * can start in this biome in any of the chunk generators used by the current world-save}
	 * <p>
	 * This method is intended for use with the Vanilla configured structures found in {@link StructureFeature}.
	 */
	default boolean validForBuiltInStructure(StructureFeature structureFeature) {
		RegistryKey<StructureFeature> key = BuiltInRegistryKeys.get(structureFeature);
		return this.validForStructure(key);
	}

	/**
	 * {@return {@code true} if the configured structure with the given key can start in this biome in any chunk generator
	 * used by the current world-save, otherwise {@code false}}
	 */
	boolean validForStructure(RegistryKey<StructureFeature> key);

	/**
	 * Tries to retrieve the registry key for the given configured feature, which should be from this biomes
	 * current structure list. May be empty if the configured feature is not registered, or does not come
	 * from this biomes feature list.
	 */
	Optional<RegistryKey<StructureFeature>> getStructureKey(StructureFeature structureFeature);

	/**
	 * Tries to determine whether this biome generates in a specific dimension, based on the {@link net.minecraft.world.gen.GeneratorOptions}
	 * used by the current world-save.
	 * <p>
	 * If no dimension options exist for the given dimension key, {@code false} is returned.
	 */
	boolean canGenerateIn(RegistryKey<DimensionOptions> dimensionKey);

	/**
	 * {@return true if this biome is in the given {@link TagKey}, otherwise {@code false}}
	 */
	boolean isIn(TagKey<Biome> tag);

	/**
	 * Returns whether the given key to a registry entry exists in the registry or not.
	 *
	 * @param registryKey the key of the registry to check in
	 * @param entryKey    the key of the registry entry to check the existence for
	 * @param <T>         the type of the registry entry
	 * @return {@code true if the registry entry exists, otherwise {@code false}}
	 */
	<T> boolean doesRegistryEntryExist(RegistryKey<? extends Registry<? extends T>> registryKey, RegistryKey<T> entryKey);

	/**
	 * {@return {@code true} if the given placed feature key exists in the registry, otherwise {@code false}}
	 *
	 * @param key the key of the placed feature
	 * @see #doesRegistryEntryExist(RegistryKey, RegistryKey)
	 */
	default boolean doesPlacedFeatureExist(RegistryKey<PlacedFeature> key) {
		return this.doesRegistryEntryExist(Registry.PLACED_FEATURE_KEY, key);
	}
}
