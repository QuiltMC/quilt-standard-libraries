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

import java.util.Arrays;
import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that removes carvers from a biome. If no steps are specified when decoding, carvers will be removed from all steps.
 * @param steps the carver generation steps to remove the carvers from
 */
public record RemoveCarversModifier(CodecAwarePredicate<BiomeSelectionContext> selector, List<RegistryKey<ConfiguredCarver<?>>> carvers, List<GenerationStep.Carver> steps) implements BiomeModifier {
	public static final Identifier IDENTIFIER = new Identifier("quilt", "remove_carvers");
	public static final Codec<RemoveCarversModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(RemoveCarversModifier::selector),
			Codec.either(RegistryKey.codec(RegistryKeys.CONFIGURED_CARVER), RegistryKey.codec(RegistryKeys.CONFIGURED_CARVER).listOf()).xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)).fieldOf("carvers").forGetter(RemoveCarversModifier::carvers),
			Codec.either(GenerationStep.Carver.CODEC, GenerationStep.Carver.CODEC.listOf()).xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)).optionalFieldOf("steps", Arrays.asList(GenerationStep.Carver.values())).forGetter(RemoveCarversModifier::steps)
	).apply(instance, RemoveCarversModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (var carver : carvers) {
			for (var step : steps) {
				modificationContext.getGenerationSettings().removeCarver(step, carver);
			}
		}
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
