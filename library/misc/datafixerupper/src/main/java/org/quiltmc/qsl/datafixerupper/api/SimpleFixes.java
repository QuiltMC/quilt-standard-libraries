/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.datafixerupper.api;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import net.minecraft.datafixer.fix.BiomeRenameFix;
import net.minecraft.datafixer.fix.BlockNameFix;
import net.minecraft.datafixer.fix.ItemNameFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides methods to add {@link DataFix}es to {@link DataFixerBuilder}s.
 */
public final class SimpleFixes {
	private SimpleFixes() { }

	/**
	 * Adds a block rename fix to the builder.
	 *
	 * @param builder the builder
	 * @param name the fix's name
	 * @param oldId the block's old ID
	 * @param newId the block's new ID
	 * @param schema the schema
	 * @see BlockNameFix
	 */
	public static void addBlockRenameFix(DataFixerBuilder builder, String name, String oldId, String newId, Schema schema) {
		checkNotNull(builder, "DataFixerBuilder cannot be null");
		checkNotNull(name, "Fix name cannot be null");
		checkNotNull(oldId, "Old ID cannot be null");
		checkNotNull(newId, "New ID cannot be null");
		checkNotNull(schema, "Schema cannot be null");
		builder.addFixer(BlockNameFix.create(schema, name, (inputName) ->
				Objects.equals(IdentifierNormalizingSchema.normalize(inputName), oldId) ? newId : inputName));
	}

	/**
	 * Adds an item rename fix to the builder.
	 *
	 * @param builder the builder
	 * @param name the fix's name
	 * @param oldId the item's old ID
	 * @param newId the item's new ID
	 * @param schema the schema
	 * @see ItemNameFix
	 */
	public static void addItemRenameFix(DataFixerBuilder builder, String name, String oldId, String newId, Schema schema) {
		checkNotNull(builder, "DataFixerBuilder cannot be null");
		checkNotNull(name, "Fix name cannot be null");
		checkNotNull(oldId, "Old ID cannot be null");
		checkNotNull(newId, "New ID cannot be null");
		checkNotNull(schema, "Schema cannot be null");
		builder.addFixer(ItemNameFix.create(schema, name, (inputName) ->
				Objects.equals(IdentifierNormalizingSchema.normalize(inputName), oldId) ? newId : inputName));
	}

	/**
	 * Adds a biome rename fix to the builder.
	 *
	 * @param builder the builder
	 * @param name the fix's name
	 * @param changes a map of old biome IDs to new biome IDs
	 * @param schema the schema
	 * @see BiomeRenameFix
	 */
	public static void addBiomeRenameFix(DataFixerBuilder builder, String name, Map<String, String> changes, Schema schema) {
		checkNotNull(builder, "DataFixerBuilder cannot be null");
		checkNotNull(name, "Fix name cannot be null");
		checkNotNull(changes, "Changes cannot be null");
		checkNotNull(schema, "Schema cannot be null");
		builder.addFixer(new BiomeRenameFix(schema, false, name, ImmutableMap.copyOf(changes)));
	}
}
