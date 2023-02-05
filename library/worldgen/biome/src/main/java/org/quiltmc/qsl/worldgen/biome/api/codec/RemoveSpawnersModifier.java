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
import java.util.Set;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

public record RemoveSpawnersModifier(CodecAwarePredicate<BiomeSelectionContext> selector,
								  Set<Identifier> entityTypes,
								  Set<SpawnGroup> groups) implements BiomeModifier {
	public static final Codec<RemoveSpawnersModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(RemoveSpawnersModifier::selector),
			Codec.either(Identifier.CODEC, Identifier.CODEC.listOf())
					.xmap(either -> either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
					.xmap(Set::copyOf, List::copyOf)
					.fieldOf("entity_types").forGetter(RemoveSpawnersModifier::entityTypes),
			Codec.either(SpawnGroup.CODEC, SpawnGroup.CODEC.listOf())
					.xmap(either -> either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
					.optionalFieldOf("groups", Arrays.asList(SpawnGroup.values()))
					.xmap(Set::copyOf, List::copyOf)
					.forGetter(RemoveSpawnersModifier::groups)
	).apply(instance, RemoveSpawnersModifier::new));
	public static final Identifier IDENTIFIER = new Identifier("quilt", "add_spawners");

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		modificationContext.getSpawnSettings().removeSpawns((group, entry) ->
				groups.contains(group) && entityTypes.contains(Registries.ENTITY_TYPE.getId(entry.type)));
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
