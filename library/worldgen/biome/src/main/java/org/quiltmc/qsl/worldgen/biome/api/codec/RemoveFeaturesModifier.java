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

package org.quiltmc.qsl.worldgen.biome.api.codec;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import org.quiltmc.qsl.data.callback.api.CodecHelpers;
import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that removes features from a biome. If no steps are specified when decoding, features will be removed from all steps.
 * <p>
 * The biome modifier identifier is {@code quilt:remove_features}.
 *
 * @param steps    the feature generation steps to remove the features from
 * @param features registry keys for the features to remove
 */
public record RemoveFeaturesModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		List<RegistryKey<PlacedFeature>> features,
		List<GenerationStep.Feature> steps
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "remove_features");
	public static final Codec<RemoveFeaturesModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(RemoveFeaturesModifier::selector),
			CodecHelpers.listOrValue(RegistryKey.codec(RegistryKeys.PLACED_FEATURE)).fieldOf("features").forGetter(RemoveFeaturesModifier::features),
			CodecHelpers.listOrValue(GenerationStep.Feature.CODEC).optionalFieldOf("steps", Arrays.asList(GenerationStep.Feature.values())).forGetter(RemoveFeaturesModifier::steps)
	).apply(instance, RemoveFeaturesModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (var feature : this.features) {
			for (var step : this.steps) {
				modificationContext.getGenerationSettings().removeFeature(step, feature);
			}
		}
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
