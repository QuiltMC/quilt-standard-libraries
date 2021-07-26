package org.quiltmc.qsl.inworldrecipes.impl;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.inworldrecipes.api.InWorldRecipe;
import org.quiltmc.qsl.inworldrecipes.api.InWorldRecipeRegistries;

import java.util.Map;

public final class InWorldRecipeMaps {
	private InWorldRecipeMaps() { throw new AssertionError(); }

	public static Multimap<Block, InWorldRecipe> sword, pickaxe, axe, shovel, hoe;

	// TODO update this to run on datapack reload
	// TODO data-drive!
	public static void update() {
		sword = fromRegistry(InWorldRecipeRegistries.SWORD);
		pickaxe = fromRegistry(InWorldRecipeRegistries.PICKAXE);
		axe = fromRegistry(InWorldRecipeRegistries.AXE);
		shovel = fromRegistry(InWorldRecipeRegistries.SHOVEL);
		hoe = fromRegistry(InWorldRecipeRegistries.HOE);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean tryPerform(@NotNull Multimap<Block, InWorldRecipe> map, @NotNull Block targetBlock, @NotNull ItemUsageContext context) {
		for (InWorldRecipe recipe : map.get(targetBlock)) {
			if (!recipe.targetBlocks().contains(targetBlock))
				continue;
			if (recipe.tryPerform(context))
				return true;
		}
		return false;
	}

	private static Multimap<Block, InWorldRecipe> fromRegistry(@NotNull Registry<InWorldRecipe> registry) {
		//noinspection UnstableApiUsage
		Multimap<Block, InWorldRecipe> map = MultimapBuilder.hashKeys().hashSetValues().build();
		for (Map.Entry<RegistryKey<InWorldRecipe>, InWorldRecipe> entry : registry.getEntries()) {
			InWorldRecipe recipe = entry.getValue();
			for (Block block : recipe.targetBlocks())
				map.put(block, recipe);
		}
		return ImmutableMultimap.copyOf(map);
	}
}
