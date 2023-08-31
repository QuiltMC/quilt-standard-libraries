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

package org.quiltmc.qsl.worldgen.biome.api.codec.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.HolderSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome selector that selects a biome if it is part of the provided holder set; useful for selecting single biomes,
 * lists of biomes, or biome tags.
 */
public record ValueBiomeSelector(HolderSet<Biome> value) implements CodecAwarePredicate<BiomeSelectionContext> {
	public static final Identifier CODEC_ID = new Identifier("quilt", "value");
	public static final Codec<ValueBiomeSelector> CODEC = RecordCodecBuilder.create(i -> i.group(
			Biome.LIST_CODEC.fieldOf("value").forGetter(ValueBiomeSelector::value)
	).apply(i, ValueBiomeSelector::new));

	@Override
	public boolean test(BiomeSelectionContext biomeSelectionContext) {
		return this.value.contains(biomeSelectionContext.getBiomeHolder());
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
