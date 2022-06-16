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

package org.quiltmc.qsl.worldgen.biome.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Holder;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.random.LegacySimpleRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.ChunkRandom;

/**
 * Internal data for modding Vanilla's {@link TheEndBiomeSource}.
 */
@ApiStatus.Internal
public final class TheEndBiomeData {
	private static final Set<RegistryKey<Biome>> BIOMES = new HashSet<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BIOMES_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_MIDLANDS_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BARRENS_MAP = new IdentityHashMap<>();

	static {
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.THE_END, key -> new WeightedPicker<>())
				.add(BiomeKeys.THE_END, 1.0);
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_HIGHLANDS, 1.0);
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.SMALL_END_ISLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.SMALL_END_ISLANDS, 1.0);

		END_MIDLANDS_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_MIDLANDS, 1.0);
		END_BARRENS_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_BARRENS, 1.0);
	}

	private TheEndBiomeData() {
	}

	public static void addEndBiomeReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double weight) {
		Preconditions.checkNotNull(replaced, "replaced entry is null");
		Preconditions.checkNotNull(variant, "variant entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BIOMES_MAP.computeIfAbsent(replaced, key -> new WeightedPicker<>()).add(variant, weight);
		BIOMES.add(variant);
	}

	public static void addEndMidlandsReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> midlands, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(midlands, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_MIDLANDS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(midlands, weight);
		BIOMES.add(midlands);
	}

	public static void addEndBarrensReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> barrens, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(barrens, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BARRENS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(barrens, weight);
		BIOMES.add(barrens);
	}

	public static Overrides createOverrides(Registry<Biome> biomeRegistry) {
		return new Overrides(biomeRegistry);
	}

	public static Collection<Holder<Biome>> getAddedBiomes(Registry<Biome> registry) {
		return BIOMES.stream().map(registry::method_44298).collect(Collectors.toSet());
	}

	/**
	 * An instance of this class is attached to each {@link TheEndBiomeSource}.
	 */
	public static class Overrides {
		// Cache for our own sampler (used for random biome replacement selection).
		private final Map<MultiNoiseUtil.MultiNoiseSampler, PerlinNoiseSampler> samplers = new WeakHashMap<>();

		// Vanilla entries to compare against
		private final Holder<Biome> endMidlands;
		private final Holder<Biome> endBarrens;
		private final Holder<Biome> endHighlands;

		// Maps where the keys have been resolved to actual entries
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBiomesMap;
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endMidlandsMap;
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBarrensMap;

		// current seed, set from ChunkGenerator hook since it is not normally available
		private static final ThreadLocal<Long> SEED = new ThreadLocal<>();

		public Overrides(Registry<Biome> biomeRegistry) {
			this.endMidlands = biomeRegistry.getHolderOrThrow(BiomeKeys.END_MIDLANDS);
			this.endBarrens = biomeRegistry.getHolderOrThrow(BiomeKeys.END_BARRENS);
			this.endHighlands = biomeRegistry.getHolderOrThrow(BiomeKeys.END_HIGHLANDS);

			this.endBiomesMap = this.resolveOverrides(biomeRegistry, END_BIOMES_MAP);
			this.endMidlandsMap = this.resolveOverrides(biomeRegistry, END_MIDLANDS_MAP);
			this.endBarrensMap = this.resolveOverrides(biomeRegistry, END_BARRENS_MAP);
		}

		// Resolves all RegistryKey instances to RegistryEntries
		private Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> resolveOverrides(Registry<Biome> biomeRegistry, Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> overrides) {
			var result = new IdentityHashMap<Holder<Biome>, WeightedPicker<Holder<Biome>>>(overrides.size());

			for (Map.Entry<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> entry : overrides.entrySet()) {
				result.put(biomeRegistry.getHolderOrThrow(entry.getKey()), entry.getValue().map(biomeRegistry::getHolderOrThrow));
			}

			return result;
		}

		public Holder<Biome> pick(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, Holder<Biome> vanillaBiome) {
			PerlinNoiseSampler sampler = this.getSampler(noise);
			Holder<Biome> replacementKey;

			// The x and z of the entry are divided by 64 to ensure custom biomes are large enough; going larger than this]
			// seems to make custom biomes too hard to find.
			if (vanillaBiome == endMidlands || vanillaBiome == endBarrens) {
				// Since the highlands picker is statically populated by InternalBiomeData, picker will never be null.
				WeightedPicker<Holder<Biome>> highlandsPicker = this.endBiomesMap.get(this.endHighlands);
				Holder<Biome> highlandsKey = highlandsPicker.pickFromNoise(sampler, x / 64.0, 0, z / 64.0);

				if (vanillaBiome == endMidlands) {
					WeightedPicker<Holder<Biome>> midlandsPicker = this.endMidlandsMap.get(highlandsKey);
					replacementKey = (midlandsPicker == null) ? vanillaBiome : midlandsPicker.pickFromNoise(sampler, x / 64.0, 0, z / 64.0);
				} else {
					WeightedPicker<Holder<Biome>> barrensPicker = this.endBarrensMap.get(highlandsKey);
					replacementKey = (barrensPicker == null) ? vanillaBiome : barrensPicker.pickFromNoise(sampler, x / 64.0, 0, z / 64.0);
				}
			} else {
				// Since the main island and small islands pickers are statically populated by InternalBiomeData, picker will never be null.
				WeightedPicker<Holder<Biome>> picker = this.endBiomesMap.get(vanillaBiome);
				replacementKey = (picker == null) ? vanillaBiome : picker.pickFromNoise(sampler, x / 64.0, 0, z / 64.0);
			}

			return replacementKey;
		}

		private synchronized PerlinNoiseSampler getSampler(MultiNoiseUtil.MultiNoiseSampler noise) {
			PerlinNoiseSampler ret = this.samplers.get(noise);

			if (ret == null) {
				Long seed = Overrides.SEED.get();
				if (seed == null) {
					seed = ((MultiNoiseSamplerExtensions) (Object) noise).quilt$getSeed();

					if (seed == null) {
						throw new IllegalStateException("seed isn't set, ChunkGenerator hook not working?");
					}
				}

				ret = new PerlinNoiseSampler(new ChunkRandom(new LegacySimpleRandom(seed)));
				this.samplers.put(noise, ret);
			}

			return ret;
		}

		public static void setSeed(long seed) {
			Overrides.SEED.set(seed);
		}
	}
}
