package org.quiltmc.qsl.worldgen.biome.impl.modification;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.slf4j.Logger;

import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceFileNamespace;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.qsl.base.api.event.data.CodecMap;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

public class BiomeModificationReloader {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	private static final Codec<Pair<ModificationPhase, BiomeModifier>> CODEC = Codecs.createLazy(() ->
			Codec.pair(ModificationPhase.CODEC.fieldOf("phase").codec(), CodecMap.createDelegatingCodec(BiomeModifier.BIOME_MODIFIER_CODECS, BiomeModifier.class)));

	private final Identifier resourcePath = new Identifier("quilt", "biome_modifiers");

	private final Map<ModificationPhase, Map<Identifier, BiomeModifier>> listeners = new HashMap<>();
	private final Map<ModificationPhase, Map<Identifier, BiomeModifier>> combinedListeners = new HashMap<>();
	public void apply(ResourceManager resourceManager, HolderLookup.Provider provider) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
		Map<ModificationPhase, Map<Identifier, BiomeModifier>> dynamicListeners = new LinkedHashMap<>();
		ResourceFileNamespace resourceFileNamespace = ResourceFileNamespace.json(this.resourcePath.getNamespace()+"/"+this.resourcePath.getPath());
		var resources = resourceFileNamespace.findMatchingResources(resourceManager).entrySet();
		for (Map.Entry<Identifier, Resource> entry : resources) {
			Identifier identifier = entry.getKey();
			Identifier unwrappedIdentifier = resourceFileNamespace.unwrapFilePath(identifier);
			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				try {
					DataResult<Pair<ModificationPhase, BiomeModifier>> result = CODEC.parse(ops, json);
					if (result.result().isPresent()) {
						var pair = result.result().get();
						dynamicListeners.computeIfAbsent(pair.getFirst(), k -> new LinkedHashMap<>()).put(unwrappedIdentifier, pair.getSecond());
					} else {
						LOGGER.error("Couldn't parse data file {} from {}: {}", unwrappedIdentifier, identifier, result.error().get().message());
					}
				} catch (IllegalStateException e) {
					// We have to catch the 'java.lang.IllegalStateException: Missing tag TagKey[minecraft:worldgen/biome / minecraft:increased_fire_burnout]'
					// that can be thrown by the biome holder list codec...
					LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, identifier, e);
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, identifier, e);
			}
		}

		updateDynamicListeners(dynamicListeners);
	}

	private void updateDynamicListeners(Map<ModificationPhase, Map<Identifier, BiomeModifier>> dynamicListeners) {
		combinedListeners.clear();
		for (ModificationPhase phase : ModificationPhase.values()) {
			ImmutableMap.Builder<Identifier, BiomeModifier> builder = ImmutableMap.builder();
			builder.putAll(listeners.getOrDefault(phase, Map.of()));
			builder.putAll(dynamicListeners.getOrDefault(phase, Map.of()));

			combinedListeners.put(phase, builder.buildKeepingLast());
		}
	}

	public Map<Identifier, BiomeModifier> getCombinedMap(ModificationPhase phase) {
		return combinedListeners.getOrDefault(phase, Map.of());
	}

	public void addModifier(ModificationPhase phase, Identifier identifier, BiomeModifier modifier) {
		listeners.computeIfAbsent(phase, k -> new HashMap<>()).put(identifier, modifier);
	}
}
