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

package org.quiltmc.qsl.worldgen.biome.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.util.MultiNoiseBiomeSourceParameterList;

/**
 * Internal data for modding Vanilla's {@link MultiNoiseBiomeSourceParameterList.Preset#NETHER}.
 */
@ApiStatus.Internal
public final class NetherBiomeData {
	// The cached sets of vanilla biomes that would generate from Vanilla's nether biome preset.
	private static final Set<RegistryKey<Biome>> VANILLA_NETHER_BIOMES = MultiNoiseBiomeSourceParameterList.Preset.NETHER
			.method_49514()
			.collect(Collectors.toSet());

	// The cached sets of all biomes, included modded ones, that would generate from Vanilla's nether biome preset.
	private static final Set<RegistryKey<Biome>> NETHER_BIOMES = new HashSet<>();

	public static final Map<RegistryKey<Biome>, MultiNoiseUtil.NoiseHypercube> NETHER_BIOME_NOISE_POINTS = new Reference2ObjectOpenHashMap<>();

	private NetherBiomeData() {}

	public static void addNetherBiome(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube spawnNoisePoint) {
		Preconditions.checkArgument(biome != null, "Biome is null");
		Preconditions.checkArgument(spawnNoisePoint != null, "MultiNoiseUtil.NoiseHypercube is null");
		NETHER_BIOME_NOISE_POINTS.put(biome, spawnNoisePoint);
		NetherBiomeData.clearBiomeSourceCache();
	}

	public static boolean canGenerateInNether(RegistryKey<Biome> biome) {
		if (NETHER_BIOMES.isEmpty()) {
			NETHER_BIOMES.addAll(VANILLA_NETHER_BIOMES);
			NETHER_BIOMES.addAll(NETHER_BIOME_NOISE_POINTS.keySet());
		}

		return NETHER_BIOMES.contains(biome);
	}

	private static void clearBiomeSourceCache() {
		NETHER_BIOMES.clear();
	}

	public static <T> MultiNoiseUtil.ParameterRangeList<T> withModdedBiomeEntries(
			MultiNoiseUtil.ParameterRangeList<T> parameterRangeList, Function<RegistryKey<Biome>, T> function
	) {
		if (NETHER_BIOME_NOISE_POINTS.isEmpty()) {
			return parameterRangeList;
		}

		var entryList = new ArrayList<>(parameterRangeList.getEntries());

		for (var entry : NETHER_BIOME_NOISE_POINTS.entrySet()) {
			entryList.add(Pair.of(entry.getValue(), function.apply(entry.getKey())));
		}

		return new MultiNoiseUtil.ParameterRangeList<>(Collections.unmodifiableList(entryList));
	}
}
