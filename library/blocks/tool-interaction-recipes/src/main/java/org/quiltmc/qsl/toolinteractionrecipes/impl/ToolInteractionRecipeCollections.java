package org.quiltmc.qsl.toolinteractionrecipes.impl;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipe;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipeCollection;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class ToolInteractionRecipeCollections {
	private final Map<Identifier, ToolInteractionRecipeCollection> map = new HashMap<>();

	private static ToolInteractionRecipeCollections instance;

	public static @NotNull ToolInteractionRecipeCollections get(@NotNull World world) {
		// TODO dynamic registry stuff
		// for now we just have a singleton
		if (instance == null) {
			instance = new ToolInteractionRecipeCollections();
			instance.map.put(new Identifier("minecraft", "sword"), fromRegistry(ToolInteractionRecipeRegistries.SWORD));
			instance.map.put(new Identifier("minecraft", "pickaxe"), fromRegistry(ToolInteractionRecipeRegistries.PICKAXE));
			instance.map.put(new Identifier("minecraft", "axe"), fromRegistry(ToolInteractionRecipeRegistries.AXE));
			instance.map.put(new Identifier("minecraft", "shovel"), fromRegistry(ToolInteractionRecipeRegistries.SHOVEL));
			instance.map.put(new Identifier("minecraft", "hoe"), fromRegistry(ToolInteractionRecipeRegistries.HOE));
		}
		return instance;
	}

	private static @NotNull ToolInteractionRecipeCollection fromRegistry(@NotNull Registry<ToolInteractionRecipe> registry) {
		ToolInteractionRecipeCollection collection = new ToolInteractionRecipeCollection();
		for (Map.Entry<RegistryKey<ToolInteractionRecipe>, ToolInteractionRecipe> entry : registry.getEntries()) {
			collection.add(entry.getValue());
		}
		return collection;
	}

	public @NotNull ToolInteractionRecipeCollection getCollection(@NotNull Identifier toolTypeId) {
		return map.computeIfAbsent(toolTypeId, id -> new ToolInteractionRecipeCollection());
	}
}
