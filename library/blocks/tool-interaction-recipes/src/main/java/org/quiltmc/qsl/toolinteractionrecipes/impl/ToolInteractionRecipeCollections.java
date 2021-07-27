package org.quiltmc.qsl.toolinteractionrecipes.impl;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipe;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipeCollection;

import java.util.Map;

public final class ToolInteractionRecipeCollections {
	public static void update() {

	}

	private static @NotNull ToolInteractionRecipeCollection fromRegistry(@NotNull Registry<ToolInteractionRecipe> registry) {
		ToolInteractionRecipeCollection collection = new ToolInteractionRecipeCollection();
		for (Map.Entry<RegistryKey<ToolInteractionRecipe>, ToolInteractionRecipe> entry : registry.getEntries()) {
			collection.add(entry.getValue());
		}
		return collection;
	}
}
