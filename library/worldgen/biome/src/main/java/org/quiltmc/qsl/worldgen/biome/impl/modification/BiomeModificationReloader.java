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

package org.quiltmc.qsl.worldgen.biome.impl.modification;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceFileNamespace;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

@ApiStatus.Internal
public class BiomeModificationReloader {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	private static final Codec<Pair<ModificationPhase, BiomeModifier>> CODEC = Codecs.createLazy(() ->
			Codec.pair(ModificationPhase.CODEC.fieldOf("phase").codec(), BiomeModifier.BIOME_MODIFIER_CODECS.createDelegatingCodec("biome modifier")));

	private final Identifier resourcePath = new Identifier("quilt", "biome_modifiers");

	private final Map<Identifier, Pair<ModificationPhase, BiomeModifier>> listeners = new HashMap<>();

	private final Map<Identifier, Pair<ModificationPhase, BiomeModifier>> combinedListeners = new HashMap<>();

	public void apply(ResourceManager resourceManager, HolderLookup.Provider provider) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
		Map<Identifier, Pair<ModificationPhase, BiomeModifier>> dynamicListeners = new LinkedHashMap<>();
		ResourceFileNamespace resourceFileNamespace = ResourceFileNamespace.json(this.resourcePath.getNamespace() + "/" + this.resourcePath.getPath());

		var resources = resourceFileNamespace.findMatchingResources(resourceManager).entrySet();
		for (Map.Entry<Identifier, Resource> entry : resources) {
			Identifier id = entry.getKey();
			Identifier unwrappedIdentifier = resourceFileNamespace.unwrapFilePath(id);

			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				try {
					DataResult<Pair<ModificationPhase, BiomeModifier>> result = CODEC.parse(ops, json);

					if (result.result().isPresent()) {
						var pair = result.result().get();
						dynamicListeners.put(unwrappedIdentifier, pair);
					} else {
						LOGGER.error("Couldn't parse data file {} from {}: {}", unwrappedIdentifier, id, result.error().get().message());
					}
				} catch (IllegalStateException e) {
					// We have to catch the 'java.lang.IllegalStateException: Missing tag TagKey[minecraft:worldgen/biome / minecraft:increased_fire_burnout]'
					// that can be thrown by the biome holder list codec...
					LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, id, e);
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, id, e);
			}
		}

		this.updateDynamicListeners(dynamicListeners);
	}

	private void updateDynamicListeners(Map<Identifier, Pair<ModificationPhase, BiomeModifier>> dynamicListeners) {
		this.combinedListeners.clear();
		this.combinedListeners.putAll(this.listeners);
		this.combinedListeners.putAll(dynamicListeners);
	}

	public Map<Identifier, BiomeModifier> getCombinedMap(ModificationPhase phase) {
		return this.combinedListeners.entrySet().stream()
				.filter(entry -> entry.getValue().getFirst() == phase)
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getSecond()));
	}

	public void addModifier(ModificationPhase phase, Identifier identifier, BiomeModifier modifier) {
		this.listeners.put(identifier, new Pair<>(phase, modifier));
	}
}
