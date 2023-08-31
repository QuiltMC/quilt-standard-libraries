/*
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

package org.quiltmc.qsl.worldgen.surface_rule.test;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

public class QuiltSurfaceRuleTest implements SurfaceRuleEvents.OverworldModifierCallback,
		SurfaceRuleEvents.NetherModifierCallback,
		SurfaceRuleEvents.TheEndModifierCallback {
	@Override
	public void modifyOverworldRules(@NotNull SurfaceRuleContext.Overworld context) {
		// When in doubt, T R A N S. Seed 7205143747332514273 is a good one for testing.
		SurfaceRules.MaterialCondition blueNoise1 = SurfaceRules.noiseThreshold(NoiseParametersKeys.CALCITE, 0.05, 0.1);
		SurfaceRules.MaterialCondition pinkNoise1 = SurfaceRules.noiseThreshold(NoiseParametersKeys.CALCITE, 0.1, 0.15);
		SurfaceRules.MaterialCondition whiteNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.CALCITE, 0.15, 0.20);
		SurfaceRules.MaterialCondition pinkNoise2 = SurfaceRules.noiseThreshold(NoiseParametersKeys.CALCITE, 0.20, 0.25);
		SurfaceRules.MaterialCondition blueNoise2 = SurfaceRules.noiseThreshold(NoiseParametersKeys.CALCITE, 0.25, 0.30);

		SurfaceRules.MaterialRule LIGHT_BLUE_CONCRETE = SurfaceRules.block(Blocks.LIGHT_BLUE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule PINK_CONCRETE = SurfaceRules.block(Blocks.PINK_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule WHITE_CONCRETE = SurfaceRules.block(Blocks.WHITE_CONCRETE.getDefaultState());

		context.materialRules().add(0,
				SurfaceRules.condition(
						SurfaceRules.abovePreliminarySurface(),
						SurfaceRules.condition(
								SurfaceRules.ON_FLOOR,
								SurfaceRules.sequence(
										SurfaceRules.condition(blueNoise1, LIGHT_BLUE_CONCRETE),
										SurfaceRules.condition(pinkNoise1, PINK_CONCRETE),
										SurfaceRules.condition(whiteNoise, WHITE_CONCRETE),
										SurfaceRules.condition(pinkNoise2, PINK_CONCRETE),
										SurfaceRules.condition(blueNoise2, LIGHT_BLUE_CONCRETE)
								)
						)
				)
		);
	}

	@Override
	public void modifyNetherRules(@NotNull SurfaceRuleContext.Nether context) {
		SurfaceRules.MaterialCondition redNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.04, -0.08);
		SurfaceRules.MaterialCondition orangeNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.8, -0.12);
		SurfaceRules.MaterialCondition yellowNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.12, -0.16);
		SurfaceRules.MaterialCondition greenNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.16, -0.20);
		SurfaceRules.MaterialCondition lightBlueNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.20, -0.24);
		SurfaceRules.MaterialCondition darkBlueNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.24, -0.28);
		SurfaceRules.MaterialCondition purpleNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, -0.28, -0.32);

		SurfaceRules.MaterialRule RED_CONCRETE = SurfaceRules.block(Blocks.RED_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule ORANGE_CONCRETE = SurfaceRules.block(Blocks.ORANGE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule YELLOW_CONCRETE = SurfaceRules.block(Blocks.YELLOW_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule GREEN_CONCRETE = SurfaceRules.block(Blocks.GREEN_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule LIGHT_BLUE_CONCRETE = SurfaceRules.block(Blocks.LIGHT_BLUE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule DARK_BLUE_CONCRETE = SurfaceRules.block(Blocks.BLUE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule PURPLE_CONCRETE = SurfaceRules.block(Blocks.PURPLE_CONCRETE.getDefaultState());

		context.materialRules().add(0,
				SurfaceRules.condition(
						SurfaceRules.UNDER_CEILING,
						SurfaceRules.sequence(
								SurfaceRules.condition(redNoise, RED_CONCRETE),
								SurfaceRules.condition(orangeNoise, ORANGE_CONCRETE),
								SurfaceRules.condition(yellowNoise, YELLOW_CONCRETE),
								SurfaceRules.condition(greenNoise, GREEN_CONCRETE),
								SurfaceRules.condition(lightBlueNoise, LIGHT_BLUE_CONCRETE),
								SurfaceRules.condition(darkBlueNoise, DARK_BLUE_CONCRETE),
								SurfaceRules.condition(purpleNoise, PURPLE_CONCRETE)
						)
				)
		);
	}

	@Override
	public void modifyTheEndRules(@NotNull SurfaceRuleContext.TheEnd context) {
		RegistryKey<Biome> TEST_END_HIGHLANDS = RegistryKey.of(RegistryKeys.BIOME, new Identifier("quilt_biome_testmod", "test_end_highlands"));
		RegistryKey<Biome> TEST_END_MIDLANDS = RegistryKey.of(RegistryKeys.BIOME, new Identifier("quilt_biome_testmod", "test_end_midlands"));
		RegistryKey<Biome> TEST_END_BARRRENS = RegistryKey.of(RegistryKeys.BIOME, new Identifier("quilt_biome_testmod", "test_end_barrens"));

		SurfaceRules.MaterialCondition pinkBiome = SurfaceRules.biome(TEST_END_HIGHLANDS);
		SurfaceRules.MaterialCondition whiteBiome = SurfaceRules.biome(TEST_END_MIDLANDS);
		SurfaceRules.MaterialCondition purpleBiome = SurfaceRules.biome(TEST_END_BARRRENS);
		SurfaceRules.MaterialCondition blackBiome = SurfaceRules.biome(Biomes.END_BARRENS);
		SurfaceRules.MaterialCondition blueBiome = SurfaceRules.biome(Biomes.THE_END);

		SurfaceRules.MaterialRule PINK_CONCRETE = SurfaceRules.block(Blocks.PINK_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule WHITE_CONCRETE = SurfaceRules.block(Blocks.WHITE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule PURPLE_CONCRETE = SurfaceRules.block(Blocks.PURPLE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule BLACK_CONCRETE = SurfaceRules.block(Blocks.BLACK_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule BLUE_CONCRETE = SurfaceRules.block(Blocks.BLUE_CONCRETE.getDefaultState());

		// genderfluEND :D
		context.materialRules().add(0,
				SurfaceRules.sequence(
						SurfaceRules.condition(pinkBiome, PINK_CONCRETE),
						SurfaceRules.condition(whiteBiome, WHITE_CONCRETE),
						SurfaceRules.condition(purpleBiome, PURPLE_CONCRETE),
						SurfaceRules.condition(blackBiome, BLACK_CONCRETE),
						SurfaceRules.condition(blueBiome, BLUE_CONCRETE)
				)
		);
	}
}
