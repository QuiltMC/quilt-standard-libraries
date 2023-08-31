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
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.data.callback.api.CodecHelpers;
import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that removes mob spawners from biomes.
 * <p>
 * The biome modifier identifier is {@code quilt:remove_spawners}.
 *
 * @param entityTypes identifiers of the entity types to remove
 * @param groups      the spawn groups to remove the spawners from; if not provided, defaults to all spawn groups
 */
public record RemoveSpawnersModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		Set<Identifier> entityTypes,
		Set<SpawnGroup> groups
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "remove_spawners");
	public static final Codec<RemoveSpawnersModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(RemoveSpawnersModifier::selector),
			CodecHelpers.listOrValue(Identifier.CODEC).xmap(Set::copyOf, List::copyOf).fieldOf("entity_types").forGetter(RemoveSpawnersModifier::entityTypes),
			CodecHelpers.listOrValue(SpawnGroup.CODEC).optionalFieldOf("groups", Arrays.asList(SpawnGroup.values()))
					.xmap(Set::copyOf, List::copyOf)
					.forGetter(RemoveSpawnersModifier::groups)
	).apply(instance, RemoveSpawnersModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		modificationContext.getSpawnSettings().removeSpawns((group, entry) ->
				this.groups.contains(group) && this.entityTypes.contains(Registries.ENTITY_TYPE.getId(entry.type)));
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
