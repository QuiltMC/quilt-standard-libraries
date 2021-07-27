package org.quiltmc.qsl.toolinteractionrecipes.impl;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipe;
import org.quiltmc.qsl.toolinteractionrecipes.api.ToolInteractionRecipeRegistries;

import java.util.Map;

public final class ToolInteractionRecipeMaps {
	private ToolInteractionRecipeMaps() { throw new AssertionError(); }

	public static Multimap<Block, ToolInteractionRecipe> sword, pickaxe, axe, shovel, hoe;

	// TODO update this to run on datapack reload
	// TODO data-drive!
	public static void update() {
		sword = fromRegistry(ToolInteractionRecipeRegistries.SWORD);
		pickaxe = fromRegistry(ToolInteractionRecipeRegistries.PICKAXE);
		axe = fromRegistry(ToolInteractionRecipeRegistries.AXE);
		shovel = fromRegistry(ToolInteractionRecipeRegistries.SHOVEL);
		hoe = fromRegistry(ToolInteractionRecipeRegistries.HOE);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean tryPerform(@NotNull Multimap<Block, ToolInteractionRecipe> map, @NotNull Block targetBlock, @NotNull ItemUsageContext context) {
		for (ToolInteractionRecipe recipe : map.get(targetBlock)) {
			if (!recipe.targetBlocks().contains(targetBlock))
				continue;
			if (recipe.tryPerform(context))
				return true;
		}
		return false;
	}

	private static Multimap<Block, ToolInteractionRecipe> fromRegistry(@NotNull Registry<ToolInteractionRecipe> registry) {
		//noinspection UnstableApiUsage
		Multimap<Block, ToolInteractionRecipe> map = MultimapBuilder.hashKeys().hashSetValues().build();
		for (Map.Entry<RegistryKey<ToolInteractionRecipe>, ToolInteractionRecipe> entry : registry.getEntries()) {
			ToolInteractionRecipe recipe = entry.getValue();
			for (Block block : recipe.targetBlocks())
				map.put(block, recipe);
		}
		return ImmutableMultimap.copyOf(map);
	}
}
