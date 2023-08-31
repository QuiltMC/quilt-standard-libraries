/*
 * Copyright 2023 The Quilt Project
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

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.AddCarversModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.AddFeaturesModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.AddSpawnersModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.ModifyEffectsModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.ModifyWeatherModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.NothingModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.RemoveCarversModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.RemoveFeaturesModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.RemoveSpawnersModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.selector.ValueBiomeSelector;

@ApiStatus.Internal
public class BiomeModifierCodecs implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		BiomeModifier.BIOME_MODIFIER_CODECS.register(NothingModifier.CODEC_ID, NothingModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(AddFeaturesModifier.CODEC_ID, AddFeaturesModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(RemoveFeaturesModifier.CODEC_ID, RemoveFeaturesModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(AddCarversModifier.CODEC_ID, AddCarversModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(RemoveCarversModifier.CODEC_ID, RemoveCarversModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(ModifyWeatherModifier.CODEC_ID, ModifyWeatherModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(ModifyEffectsModifier.CODEC_ID, ModifyEffectsModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(AddSpawnersModifier.CODEC_ID, AddSpawnersModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(RemoveSpawnersModifier.CODEC_ID, RemoveSpawnersModifier.CODEC);

		BiomeModifier.BIOME_SELECTOR_CODECS.register(ValueBiomeSelector.CODEC_ID, ValueBiomeSelector.CODEC);
	}
}
