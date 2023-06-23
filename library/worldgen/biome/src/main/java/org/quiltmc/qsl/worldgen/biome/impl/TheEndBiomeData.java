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

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

/**
 * Internal data for modding Vanilla's {@link TheEndBiomeSource}.
 */
@ApiStatus.Internal
public final class TheEndBiomeData implements RegistryEvents.DynamicRegistryLoadedCallback, ServerLifecycleEvents.Stopped {
	private static final Set<RegistryKey<Biome>> BIOMES = new HashSet<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BIOMES_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_MIDLANDS_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BARRENS_MAP = new IdentityHashMap<>();
	private static HolderProvider<Biome> biomeRegistry;

	static {
		END_BIOMES_MAP.computeIfAbsent(Biomes.THE_END, key -> new WeightedPicker<>())
				.add(Biomes.THE_END, 1.0);
		END_BIOMES_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_HIGHLANDS, 1.0);
		END_BIOMES_MAP.computeIfAbsent(Biomes.SMALL_END_ISLANDS, key -> new WeightedPicker<>())
				.add(Biomes.SMALL_END_ISLANDS, 1.0);

		END_MIDLANDS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_MIDLANDS, 1.0);
		END_BARRENS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_BARRENS, 1.0);
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

	public static Overrides createOverrides() {
		return new Overrides(biomeRegistry);
	}

	public static Collection<Holder<Biome>> getAddedBiomes(HolderProvider<Biome> registry) {
		return BIOMES.stream().map(registry::getHolderOrThrow).collect(Collectors.toSet());
	}

	@Override
	public void exitServer(MinecraftServer server) {
		biomeRegistry = null;
	}

	@Override
	public void onDynamicRegistryLoaded(@NotNull DynamicRegistryManager registryManager) {
		registryManager.getOptional(RegistryKeys.BIOME).ifPresent(registry -> biomeRegistry = registry.asLookup());
	}

	/**
	 * An instance of this class is attached to each {@link TheEndBiomeSource}.
	 */
	public static class Overrides {
		// Biome holders to add on the biome list
		private final Collection<Holder<Biome>> addedBiomes;

		// Vanilla holder to compare against
		private final Holder<Biome> endMidlands;
		private final Holder<Biome> endBarrens;
		private final Holder<Biome> endHighlands;

		// Maps where the keys have been resolved to actual holders
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBiomesMap;
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endMidlandsMap;
		private final Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBarrensMap;

		public Overrides(HolderProvider<Biome> biomeRegistry) {
			this.addedBiomes = TheEndBiomeData.getAddedBiomes(biomeRegistry);
			this.endMidlands = biomeRegistry.getHolderOrThrow(Biomes.END_MIDLANDS);
			this.endBarrens = biomeRegistry.getHolderOrThrow(Biomes.END_BARRENS);
			this.endHighlands = biomeRegistry.getHolderOrThrow(Biomes.END_HIGHLANDS);

			this.endBiomesMap = this.resolveOverrides(biomeRegistry, END_BIOMES_MAP, Biomes.THE_END);
			this.endMidlandsMap = this.resolveOverrides(biomeRegistry, END_MIDLANDS_MAP, Biomes.END_MIDLANDS);
			this.endBarrensMap = this.resolveOverrides(biomeRegistry, END_BARRENS_MAP, Biomes.END_BARRENS);
		}

		// Resolves all RegistryKey instances to Holders
		private Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> resolveOverrides(HolderProvider<Biome> biomeRegistry, Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> overrides, RegistryKey<Biome> vanillaKey) {
			Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> result = new Object2ObjectOpenCustomHashMap<>(overrides.size(), HolderHashStrategy.INSTANCE);

			for (Map.Entry<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> entry : overrides.entrySet()) {
				var picker = entry.getValue();
				int count = picker.getEntryCount();
				if (count == 0 || (count == 1 && entry.getKey() == vanillaKey)) continue;

				result.put(biomeRegistry.getHolderOrThrow(entry.getKey()), picker.map(biomeRegistry::getHolderOrThrow));
			}

			return result;
		}

		public Holder<Biome> pick(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, Holder<Biome> vanillaBiome) {
			boolean isMidlands = vanillaBiome.matches(this.endMidlands::isRegistryKey);

			if (isMidlands || vanillaBiome.matches(this.endBarrens::isRegistryKey)) {
				// select a random highlands biome replacement, then try to replace it with a midlands or barrens biome replacement.
				var highlandsReplacement = this.pick(this.endHighlands, this.endHighlands, this.endBiomesMap, x, z, noise);
				Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> map = isMidlands ? this.endMidlandsMap : this.endBarrensMap;

				return this.pick(highlandsReplacement, vanillaBiome, map, x, z, noise);
			} else {
				assert END_BIOMES_MAP.containsKey(vanillaBiome.getKey().orElseThrow());

				return this.pick(vanillaBiome, vanillaBiome, this.endBiomesMap, x, z, noise);
			}
		}

		private <T extends Holder<Biome>> T pick(T key, T defaultValue, Map<T, WeightedPicker<T>> pickers, int x, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
			if (pickers == null) return defaultValue;

			WeightedPicker<T> picker = pickers.get(key);
			if (picker == null) return defaultValue;
			int count = picker.getEntryCount();
			if (count == 0 || (count == 1 && key.matches(this.endHighlands::isRegistryKey))) return defaultValue;

			// The X and Z of the entry are divided by 64 to ensure custom biomes are large enough; going larger than this
			// seems to make custom biomes too hard to find.
			return picker.pickFromNoise(
					((MultiNoiseSamplerExtensions) (Object) noise).quilt$getTheEndBiomesSampler(),
					x / 64.0, 0, z / 64.0
			);
		}

		public Collection<Holder<Biome>> getAddedBiomes() {
			return this.addedBiomes;
		}
	}

	enum HolderHashStrategy implements Hash.Strategy<Holder<?>> {
		INSTANCE;

		@Override
		public boolean equals(Holder<?> a, Holder<?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			if (a.getKind() != b.getKind()) return false;
			// This Optional#get is safe - if a has key, b should also have key
			// given a.getType() != b.getType() check above
			// noinspection OptionalGetWithoutIsPresent
			return a.unwrap().map(key -> b.getKey().get() == key, b.value()::equals);
		}

		@Override
		public int hashCode(Holder<?> a) {
			if (a == null) return 0;
			return a.unwrap().map(System::identityHashCode, Object::hashCode);
		}
	}
}
