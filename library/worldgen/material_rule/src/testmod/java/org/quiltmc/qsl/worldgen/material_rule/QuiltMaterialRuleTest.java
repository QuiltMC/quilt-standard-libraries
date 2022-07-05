/*
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

package org.quiltmc.qsl.worldgen.material_rule;

import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.worldgen.material_rule.api.MaterialRuleRegistrationEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuiltMaterialRuleTest implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		SurfaceRules.MaterialCondition blueNoise1 = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.20, 0.22);
		SurfaceRules.MaterialCondition pinkNoise1 = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.22, 0.24);
		SurfaceRules.MaterialCondition whiteNoise = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.24, 0.26);
		SurfaceRules.MaterialCondition pinkNoise2 = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.26, 0.28);
		SurfaceRules.MaterialCondition blueNoise2 = SurfaceRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.28, 0.3);

		SurfaceRules.MaterialRule BLUE_CONCRETE = SurfaceRules.block(Blocks.BLUE_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule PINK_CONCRETE = SurfaceRules.block(Blocks.PINK_CONCRETE.getDefaultState());
		SurfaceRules.MaterialRule WHITE_CONCRETE = SurfaceRules.block(Blocks.WHITE_CONCRETE.getDefaultState());

		//always, always make it trans.
		MaterialRuleRegistrationEvents.NETHER_RULE_INIT.register(((materialRules) -> materialRules.add(
				SurfaceRules.condition(
						SurfaceRules.biome(BiomeKeys.PLAINS),
						SurfaceRules.condition(
								SurfaceRules.abovePreliminarySurface(),
								SurfaceRules.sequence(
										SurfaceRules.condition(blueNoise1,BLUE_CONCRETE),
										SurfaceRules.condition(pinkNoise1,PINK_CONCRETE),
										SurfaceRules.condition(whiteNoise,WHITE_CONCRETE),
										SurfaceRules.condition(pinkNoise2,PINK_CONCRETE),
										SurfaceRules.condition(blueNoise2,BLUE_CONCRETE)
								)
						)
				)
		)));
	}
}
