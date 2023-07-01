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

package org.quiltmc.qsl.worldgen.biome;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.TheNetherBiomeCreator;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.decorator.BiomePlacementModifier;
import net.minecraft.world.gen.decorator.InSquarePlacementModifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.EndPlacedFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.registry.api.event.DynamicRegistryManagerSetupContext;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;
import org.quiltmc.qsl.worldgen.biome.api.NetherBiomes;
import org.quiltmc.qsl.worldgen.biome.api.TheEndBiomes;

/**
 * <b>NOTES FOR TESTING:</b>
 * When running with this test-mod, also test this when running a dedicated server since there
 * are significant differences between server + client and how they sync biomes.
 * <p>
 * In-game, you can use {@code /locate biome} since we use nether- and end-biomes in the overworld,
 * and vice-versa, making them easy to find to verify the injection worked.
 * <p>
 * If you don't find a biome right away, teleport far away (~10000 blocks) from spawn and try again.
 */
public class QuiltBiomeTest implements ModInitializer {
	private static final Logger BIOME_TEST_LOGGER = LoggerFactory.getLogger("QuiltBiome|QuiltBiomeTest");
	public static final String NAMESPACE = "quilt_biome_testmod";

	private static final RegistryKey<Biome> TEST_CRIMSON_FOREST = RegistryKey.of(RegistryKeys.BIOME, id("test_crimson_forest"));
	private static final RegistryKey<Biome> CUSTOM_PLAINS = RegistryKey.of(RegistryKeys.BIOME, id("custom_plains"));
	private static final RegistryKey<Biome> TEST_END_HIGHLANDS = RegistryKey.of(RegistryKeys.BIOME, id("test_end_highlands"));
	private static final RegistryKey<Biome> TEST_END_MIDLANDS = RegistryKey.of(RegistryKeys.BIOME, id("test_end_midlands"));
	private static final RegistryKey<Biome> TEST_END_BARRRENS = RegistryKey.of(RegistryKeys.BIOME, id("test_end_barrens"));

	private static final Identifier QUILT_DESERT_WELL = id("quilt_desert_well");
	private static final RegistryKey<PlacedFeature> QUILT_DESERT_WELL_FEATURE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, QUILT_DESERT_WELL);
	private static final RegistryKey<PlacedFeature> MOSS_PILE_PLACED_FEATURE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("moss_pile"));

	@Override
	public void onInitialize(ModContainer mod) {
		ResourceLoader.registerBuiltinResourcePack(id("registry_entry_existence_test"), mod, ResourcePackActivationType.NORMAL);

		Preconditions.checkArgument(NetherBiomes.canGenerateInNether(Biomes.NETHER_WASTES));
		Preconditions.checkArgument(!NetherBiomes.canGenerateInNether(Biomes.END_HIGHLANDS));

		RegistryEvents.DYNAMIC_REGISTRY_SETUP.register(context -> {
			context.withRegistries(registries -> {
				HolderLookup.RegistryLookup<PlacedFeature> placedFeatureRegistryLookup = context.registryManager().getLookupOrThrow(RegistryKeys.PLACED_FEATURE);
				HolderLookup.RegistryLookup<ConfiguredCarver<?>> carverRegistryLookup = context.registryManager().getLookupOrThrow(RegistryKeys.CONFIGURED_CARVER);

				context.register(RegistryKeys.BIOME, CUSTOM_PLAINS.getValue(), () -> OverworldBiomeCreator.createPlains(
						placedFeatureRegistryLookup, carverRegistryLookup,
						false, false, false
				));

				context.register(RegistryKeys.BIOME, TEST_CRIMSON_FOREST.getValue(), () -> TheNetherBiomeCreator.createCrimsonForest(
						placedFeatureRegistryLookup, carverRegistryLookup
				));

				context.register(RegistryKeys.BIOME, TEST_END_HIGHLANDS.getValue(), () -> createEndHighlands(context));
				context.register(RegistryKeys.BIOME, TEST_END_MIDLANDS.getValue(), () -> createEndMidlands(context));
				context.register(RegistryKeys.BIOME, TEST_END_BARRRENS.getValue(), () -> createEndBarrens(context));
			}, Set.of(RegistryKeys.BIOME, RegistryKeys.PLACED_FEATURE, RegistryKeys.CONFIGURED_CARVER));

			context.withRegistries(registries -> {
				var configuredRegistry = registries.get(RegistryKeys.CONFIGURED_FEATURE);
				ConfiguredFeature<?, ?> commonDesertWell = new ConfiguredFeature<>(Feature.DESERT_WELL, DefaultFeatureConfig.INSTANCE);
				Registry.register(configuredRegistry, QUILT_DESERT_WELL, commonDesertWell);
				Holder<ConfiguredFeature<?, ?>> featureEntry = configuredRegistry
						.getHolder(configuredRegistry.getKey(commonDesertWell).orElseThrow()).orElseThrow();

				// The placement config is taken from the vanilla desert well, but no randomness
				PlacedFeature placedDesertWell = new PlacedFeature(featureEntry, List.of(InSquarePlacementModifier.getInstance(), PlacedFeatureUtil.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.getInstance()));
				registries.register(RegistryKeys.PLACED_FEATURE, QUILT_DESERT_WELL, placedDesertWell);
			}, Set.of(RegistryKeys.PLACED_FEATURE, RegistryKeys.CONFIGURED_FEATURE));
		});

		// Important for testing NetherBiomes.canGenerateInNether itself. Biome is already covered by auto-testing
		Preconditions.checkArgument(!NetherBiomes.canGenerateInNether(TEST_CRIMSON_FOREST));

		NetherBiomes.addNetherBiome(Biomes.PLAINS, MultiNoiseUtil.createNoiseHypercube(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F));
		NetherBiomes.addNetherBiome(TEST_CRIMSON_FOREST, MultiNoiseUtil.createNoiseHypercube(0.0F, -0.15F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2F));

		Preconditions.checkArgument(NetherBiomes.canGenerateInNether(TEST_CRIMSON_FOREST));

		// TESTING HINT: to get to the end:
		// /execute in minecraft:the_end run tp @s 0 90 0
		TheEndBiomes.addHighlandsBiome(Biomes.PLAINS, 5.0);
		TheEndBiomes.addHighlandsBiome(TEST_END_HIGHLANDS, 5.0);
		TheEndBiomes.addMidlandsBiome(TEST_END_HIGHLANDS, TEST_END_MIDLANDS, 10.0);
		TheEndBiomes.addBarrensBiome(TEST_END_HIGHLANDS, TEST_END_BARRRENS, 10.0);

		BiomeModifications.create(new Identifier("quilt:testmod"))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.foundInOverworld(),
						modification -> modification.getWeather().setDownfall(100))
				// Check for an excess of desert wells.
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.includeByKey(Biomes.DESERT),
						context -> context.getGenerationSettings().addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION,
								QUILT_DESERT_WELL_FEATURE
						))
				// It should be glaringly obvious if these three tests work or not; be sure to check forests as well.
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.isIn(BiomeTags.JUNGLE),
						context -> context.getEffects().setSkyColor(0x111111))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.isIn(BiomeTags.JUNGLE),
						context -> context.getEffects().setFogColor(0x000099))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.isIn(BiomeTags.FOREST),
						context -> context.getEffects().setFogColor(0x990000));

		// Allows checking very clearly if on world-creation the data-pack contents are properly loaded.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("concrete_pile"))
		);

		// Make sure data packs can define biomes
		NetherBiomes.addNetherBiome(
				RegistryKey.of(RegistryKeys.BIOME, id("example_biome")),
				MultiNoiseUtil.createNoiseHypercube(1.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.3f)
		);
		TheEndBiomes.addHighlandsBiome(
				RegistryKey.of(RegistryKeys.BIOME, id("example_biome")),
				5.0
		);

		// Will show results if the included data-pack is enabled.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld().and(context -> context.doesPlacedFeatureExist(MOSS_PILE_PLACED_FEATURE)),
				GenerationStep.Feature.VEGETAL_DECORATION,
				MOSS_PILE_PLACED_FEATURE
		);

		ServerLifecycleEvents.READY.register(server -> {
			var netherWorld = server.getWorld(World.NETHER);
			var endWorld = server.getWorld(World.END);

			assert netherWorld != null;
			assert endWorld != null;

			var pos = new BlockPos(0, 90, 0);

			checkBiomeExists(netherWorld, pos, TEST_CRIMSON_FOREST);

			checkBiomeExists(endWorld, pos, TEST_END_HIGHLANDS);
			checkBiomeExists(endWorld, pos, TEST_END_MIDLANDS);
			checkBiomeExists(endWorld, pos, TEST_END_BARRRENS);
		});
	}

	private static void checkBiomeExists(ServerWorld world, BlockPos pos, RegistryKey<Biome> biomeKey) {
		var posOfBiome = world.locateBiome((holder) -> holder.isRegistryKey(biomeKey), pos, 6400, 32, 64);

		if (posOfBiome != null) {
			BIOME_TEST_LOGGER.info("Biome {} has been found at {}.", posOfBiome.getSecond().getKey().orElseThrow(), posOfBiome.getFirst());
		} else {
			BIOME_TEST_LOGGER.error("Failed to locate biome {}. Something is probably very wrong.", biomeKey);
			throw new AssertionError("Could not locate biome " + biomeKey);
		}
	}

	// These are used for testing the spacing of custom end biomes.
	private static Biome createEndHighlands(DynamicRegistryManagerSetupContext context) {
		GenerationSettings.Builder builder = new GenerationSettings.Builder(
				context.registryManager().getLookupOrThrow(RegistryKeys.PLACED_FEATURE),
				context.registryManager().getLookupOrThrow(RegistryKeys.CONFIGURED_CARVER)
		).feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_GATEWAY_RETURN);
		return composeEndSpawnSettings(builder);
	}

	public static Biome createEndMidlands(DynamicRegistryManagerSetupContext context) {
		GenerationSettings.Builder builder = new GenerationSettings.Builder(
				context.registryManager().getLookupOrThrow(RegistryKeys.PLACED_FEATURE),
				context.registryManager().getLookupOrThrow(RegistryKeys.CONFIGURED_CARVER)
		);
		return composeEndSpawnSettings(builder);
	}

	public static Biome createEndBarrens(DynamicRegistryManagerSetupContext context) {
		GenerationSettings.Builder builder = new GenerationSettings.Builder(
				context.registryManager().getLookupOrThrow(RegistryKeys.PLACED_FEATURE),
				context.registryManager().getLookupOrThrow(RegistryKeys.CONFIGURED_CARVER)
		);
		return composeEndSpawnSettings(builder);
	}

	private static Biome composeEndSpawnSettings(GenerationSettings.Builder builder) {
		SpawnSettings.Builder builder2 = new SpawnSettings.Builder();
		DefaultBiomeFeatures.addPlainsMobs(builder2);
		return (new Biome.Builder())
				.temperature(0.5F).downfall(0.5F)
				.effects((new BiomeEffects.Builder())
						.waterColor(0x129900)
						.waterFogColor(0x121212).fogColor(0x990000).skyColor(0).moodSound(BiomeMoodSound.CAVE)
						.build())
				.spawnSettings(builder2.build())
				.generationSettings(builder.build())
				.build();
	}

	private static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}
}
