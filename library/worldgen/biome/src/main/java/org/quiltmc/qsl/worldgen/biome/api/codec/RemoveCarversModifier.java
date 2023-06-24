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
import net.minecraft.world.gen.carver.ConfiguredCarver;

import org.quiltmc.qsl.data.callback.api.CodecHelpers;
import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that removes carvers from a biome. If no steps are specified when decoding, carvers will be removed from all steps.
 * <p>
 * The biome modifier identifier is {@code quilt:remove_carvers}.
 *
 * @param steps   the carver generation steps to remove the carvers from
 * @param carvers registry keys for the carvers to remove
 */
public record RemoveCarversModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		List<RegistryKey<ConfiguredCarver<?>>> carvers,
		List<GenerationStep.Carver> steps
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "remove_carvers");
	public static final Codec<RemoveCarversModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(RemoveCarversModifier::selector),
			CodecHelpers.listOrValue(RegistryKey.codec(RegistryKeys.CONFIGURED_CARVER)).fieldOf("carvers").forGetter(RemoveCarversModifier::carvers),
			CodecHelpers.listOrValue(GenerationStep.Carver.CODEC).optionalFieldOf("steps", Arrays.asList(GenerationStep.Carver.values())).forGetter(RemoveCarversModifier::steps)
	).apply(instance, RemoveCarversModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (var carver : this.carvers) {
			for (var step : this.steps) {
				modificationContext.getGenerationSettings().removeCarver(step, carver);
			}
		}
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
