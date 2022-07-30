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

package org.quiltmc.qsl.worldgen.biome;

import java.util.List;

import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.tag.BiomeTags;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.TheNetherBiomeCreator;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.GenerationStep;
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
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;
import org.quiltmc.qsl.worldgen.biome.api.NetherBiomes;
import org.quiltmc.qsl.worldgen.biome.api.TheEndBiomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>NOTES FOR TESTING:</b>
 * When running with this test-mod, also test this when running a dedicated server since there
 * are significant differences between server + client and how they sync biomes.
 * <p>
 * Ingame, you can use {@code /locate biome} since we use nether- and end-biomes in the overworld,
 * and vice-versa, making them easy to find to verify the injection worked.
 * <p>
 * If you don't find a biome right away, teleport far away (~10000 blocks) from spawn and try again.
 */
public class QuiltBiomeTest implements ModInitializer {
	private static final Logger BIOME_TEST_LOGGER = LoggerFactory.getLogger("QuiltBiome|QuiltBiomeTest");
	public static final String NAMESPACE = "quilt_biome_testmod";

	private static final RegistryKey<Biome> TEST_CRIMSON_FOREST = RegistryKey.of(Registry.BIOME_KEY, id("test_crimson_forest"));
	private static final RegistryKey<Biome> CUSTOM_PLAINS = RegistryKey.of(Registry.BIOME_KEY, id("custom_plains"));
	private static final RegistryKey<Biome> TEST_END_HIGHLANDS = RegistryKey.of(Registry.BIOME_KEY, id("test_end_highlands"));
	private static final RegistryKey<Biome> TEST_END_MIDLANDS = RegistryKey.of(Registry.BIOME_KEY, id("test_end_midlands"));
	private static final RegistryKey<Biome> TEST_END_BARRRENS = RegistryKey.of(Registry.BIOME_KEY, id("test_end_barrens"));

	private static final RegistryKey<PlacedFeature> MOSS_PILE_PLACED_FEATURE = RegistryKey.of(Registry.PLACED_FEATURE_KEY, id("moss_pile"));

	@Override
	public void onInitialize(ModContainer mod) {
		ResourceLoader.registerBuiltinResourcePack(id("registry_entry_existence_test"), mod, ResourcePackActivationType.NORMAL);

		Registry.register(BuiltinRegistries.BIOME, TEST_CRIMSON_FOREST.getValue(), TheNetherBiomeCreator.createCrimsonForest());

		NetherBiomes.addNetherBiome(BiomeKeys.PLAINS, MultiNoiseUtil.createNoiseHypercube(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F));
		NetherBiomes.addNetherBiome(TEST_CRIMSON_FOREST, MultiNoiseUtil.createNoiseHypercube(0.0F, -0.15F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2F));

		Registry.register(BuiltinRegistries.BIOME, CUSTOM_PLAINS.getValue(), OverworldBiomeCreator.createPlains(false, false, false));

		Registry.register(BuiltinRegistries.BIOME, TEST_END_HIGHLANDS.getValue(), createEndHighlands());
		Registry.register(BuiltinRegistries.BIOME, TEST_END_MIDLANDS.getValue(), createEndMidlands());
		Registry.register(BuiltinRegistries.BIOME, TEST_END_BARRRENS.getValue(), createEndBarrens());

		// TESTING HINT: to get to the end:
		// /execute in minecraft:the_end run tp @s 0 90 0
		TheEndBiomes.addHighlandsBiome(BiomeKeys.PLAINS, 5.0);
		TheEndBiomes.addHighlandsBiome(TEST_END_HIGHLANDS, 5.0);
		TheEndBiomes.addMidlandsBiome(TEST_END_HIGHLANDS, TEST_END_MIDLANDS, 10.0);
		TheEndBiomes.addBarrensBiome(TEST_END_HIGHLANDS, TEST_END_BARRRENS, 10.0);

		ConfiguredFeature<?, ?> COMMON_DESERT_WELL = new ConfiguredFeature<>(Feature.DESERT_WELL, DefaultFeatureConfig.INSTANCE);
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id("quilt_desert_well"), COMMON_DESERT_WELL);
		Holder<ConfiguredFeature<?, ?>> featureEntry = BuiltinRegistries.CONFIGURED_FEATURE.getOrCreateHolder(BuiltinRegistries.CONFIGURED_FEATURE.getKey(COMMON_DESERT_WELL).orElseThrow()).getOrThrow(false, BIOME_TEST_LOGGER::error);

		// The placement config is taken from the vanilla desert well, but no randomness
		PlacedFeature PLACED_COMMON_DESERT_WELL = new PlacedFeature(featureEntry, List.of(InSquarePlacementModifier.getInstance(), PlacedFeatureUtil.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.getInstance()));
		Registry.register(BuiltinRegistries.PLACED_FEATURE, id("quilt_desert_well"), PLACED_COMMON_DESERT_WELL);

		BiomeModifications.create(new Identifier("quilt:testmod"))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.foundInOverworld(),
						modification -> modification.getWeather().setDownfall(100))
				// Check for an excess of desert wells.
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.includeByKey(BiomeKeys.DESERT),
						context -> context.getGenerationSettings().addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION,
								BuiltinRegistries.PLACED_FEATURE.getKey(PLACED_COMMON_DESERT_WELL).orElseThrow()
						))
				// It should be glaringly obvious if these three tests work or not; be sure to check forests as well.
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.foundInOverworld(),
						context -> context.getEffects().setSkyColor(0x111111))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.foundInOverworld(),
						context -> context.getEffects().setFogColor(0x000099))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.isIn(BiomeTags.IS_FOREST),
						context -> context.getEffects().setFogColor(0x990000));

		// Allows checking very clearly if on world-creation the data-pack contents are properly loaded.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, id("concrete_pile"))
		);

		// Make sure data packs can define biomes
		NetherBiomes.addNetherBiome(
				RegistryKey.of(Registry.BIOME_KEY, id("example_biome")),
				MultiNoiseUtil.createNoiseHypercube(1.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.3f)
		);
		TheEndBiomes.addHighlandsBiome(
				RegistryKey.of(Registry.BIOME_KEY, id("example_biome")),
				10.0
		);

		// Will show results if the included data-pack is enabled.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld().and(context -> context.doesPlacedFeatureExist(MOSS_PILE_PLACED_FEATURE)),
				GenerationStep.Feature.VEGETAL_DECORATION,
				MOSS_PILE_PLACED_FEATURE
		);
	}

	// These are used for testing the spacing of custom end biomes.
	private static Biome createEndHighlands() {
		GenerationSettings.Builder builder = new GenerationSettings.Builder()
				.feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_GATEWAY_RETURN);
		return composeEndSpawnSettings(builder);
	}

	public static Biome createEndMidlands() {
		GenerationSettings.Builder builder = (new GenerationSettings.Builder());
		return composeEndSpawnSettings(builder);
	}

	public static Biome createEndBarrens() {
		GenerationSettings.Builder builder = (new GenerationSettings.Builder());
		return composeEndSpawnSettings(builder);
	}

	private static Biome composeEndSpawnSettings(GenerationSettings.Builder builder) {
		SpawnSettings.Builder builder2 = new SpawnSettings.Builder();
		DefaultBiomeFeatures.addPlainsMobs(builder2);
		return (new Biome.Builder()).precipitation(Biome.Precipitation.NONE).temperature(0.5F).downfall(0.5F).effects((new BiomeEffects.Builder()).waterColor(0x129900).waterFogColor(0x121212).fogColor(0x990000).skyColor(0).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(builder2.build()).generationSettings(builder.build()).build();
	}

	private static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}
}
