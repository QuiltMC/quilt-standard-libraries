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
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.SpawnSettings;

import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

public record AddSpawnersModifier(CodecAwarePredicate<BiomeSelectionContext> selector,
								  List<SpawnSettings.SpawnEntry> spawners,
								  Optional<SpawnGroup> group) implements BiomeModifier {
	public static final Codec<AddSpawnersModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(AddSpawnersModifier::selector),
			Codec.either(SpawnSettings.SpawnEntry.CODEC, SpawnSettings.SpawnEntry.CODEC.listOf())
					.xmap(either -> either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
					.fieldOf("spawners").forGetter(AddSpawnersModifier::spawners),
			SpawnGroup.CODEC.optionalFieldOf("group").forGetter(AddSpawnersModifier::group)
	).apply(instance, AddSpawnersModifier::new));
	public static final Identifier IDENTIFIER = new Identifier("quilt", "add_spawners");

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (SpawnSettings.SpawnEntry spawner : spawners) {
			modificationContext.getSpawnSettings().addSpawn(group.orElseGet(spawner.type::getSpawnGroup), spawner);
		}
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
