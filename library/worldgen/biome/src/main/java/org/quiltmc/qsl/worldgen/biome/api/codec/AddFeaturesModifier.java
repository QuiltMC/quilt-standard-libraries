/*
 * Copyright 2023 QuiltMC
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

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import org.quiltmc.qsl.base.api.event.data.CodecHelpers;
import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that adds features to a biome.
 * @param step the feature generation step to add the features to
 */
public record AddFeaturesModifier(CodecAwarePredicate<BiomeSelectionContext> selector, List<RegistryKey<PlacedFeature>> features, GenerationStep.Feature step) implements BiomeModifier {
	public static final Identifier IDENTIFIER = new Identifier("quilt", "add_features");
	public static final Codec<AddFeaturesModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(AddFeaturesModifier::selector),
			CodecHelpers.listOrValue(RegistryKey.codec(RegistryKeys.PLACED_FEATURE)).fieldOf("features").forGetter(AddFeaturesModifier::features),
			GenerationStep.Feature.CODEC.fieldOf("step").forGetter(AddFeaturesModifier::step)
	).apply(instance, AddFeaturesModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (var feature : features) {
			modificationContext.getGenerationSettings().addFeature(step, feature);
		}
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
