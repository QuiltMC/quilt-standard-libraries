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

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.SpawnSettings;

import org.quiltmc.qsl.data.callback.api.CodecHelpers;
import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that adds mob spawners to biomes.
 * <p>
 * The biome modifier identifier is {@code quilt:add_spawners}.
 *
 * @param spawners the spawners to add
 * @param group    the spawn group to add the spawners to; if not provided, this is determined by the spawner's entity
 *                 type's spawn group
 */
public record AddSpawnersModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		List<SpawnSettings.SpawnEntry> spawners,
		Optional<SpawnGroup> group
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "add_spawners");
	public static final Codec<AddSpawnersModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(AddSpawnersModifier::selector),
			CodecHelpers.listOrValue(SpawnSettings.SpawnEntry.CODEC).fieldOf("spawners").forGetter(AddSpawnersModifier::spawners),
			SpawnGroup.CODEC.optionalFieldOf("group").forGetter(AddSpawnersModifier::group)
	).apply(instance, AddSpawnersModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (SpawnSettings.SpawnEntry spawner : this.spawners) {
			modificationContext.getSpawnSettings().addSpawn(this.group.orElseGet(spawner.type::getSpawnGroup), spawner);
		}
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
