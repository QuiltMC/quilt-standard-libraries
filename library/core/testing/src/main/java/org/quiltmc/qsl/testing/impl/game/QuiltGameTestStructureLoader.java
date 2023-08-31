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

package org.quiltmc.qsl.testing.impl.game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.ResourceFileNamespace;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public final class QuiltGameTestStructureLoader {
	private static final String GAME_TEST_STRUCTURE_PATH = "game_test/structures";
	private static final ResourceFileNamespace GAME_TEST_STRUCTURE_NAMESPACE = new ResourceFileNamespace(GAME_TEST_STRUCTURE_PATH, ".snbt");

	public static Stream<Identifier> streamTemplatesFromResource(ResourceManager resourceManager) {
		return GAME_TEST_STRUCTURE_NAMESPACE.findMatchingResources(resourceManager)
				.keySet().stream()
				.map(GAME_TEST_STRUCTURE_NAMESPACE::unwrapFilePath);
	}

	/**
	 * Loads the game test structure NBT from the given identifier and resource manager.
	 * @param resourceManager the resource manager for resource access
	 * @param id the identifier of the structure to load
	 * @return the NBT of the structure if present, or {@link Optional#empty()} otherwise
	 */
	public static Optional<NbtCompound> loadStructure(ResourceManager resourceManager, Identifier id) {
		var structureId = GAME_TEST_STRUCTURE_NAMESPACE.wrapToFilePath(id);

		return resourceManager.getResource(structureId)
				.map(resource -> {
					try {
						String snbt;

						try (var inputStream = resource.open()) {
							snbt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
						}

						return NbtHelper.fromSnbt(snbt);
					} catch (IOException | CommandSyntaxException e) {
						throw new RuntimeException("Error while trying to load structure: " + structureId, e);
					}
				});
	}

	private QuiltGameTestStructureLoader() {
		throw new UnsupportedOperationException("QuiltGameTestStructureLoader only contains static definitions.");
	}
}
