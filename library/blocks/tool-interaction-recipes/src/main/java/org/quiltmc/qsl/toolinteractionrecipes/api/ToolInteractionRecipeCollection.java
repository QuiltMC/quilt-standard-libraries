package org.quiltmc.qsl.toolinteractionrecipes.api;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a collection of {@link ToolInteractionRecipe}s - a convenient way to contain and invoke all recipes of
 * a specific tool type's interaction.
 */
public final class ToolInteractionRecipeCollection {
	private final Multimap<Block, ToolInteractionRecipe> multimap;
	private final List<ToolInteractionRecipe> targetAllList;

	/**
	 * Creates a recipe collection.
	 */
	@SuppressWarnings("UnstableApiUsage")
	public ToolInteractionRecipeCollection() {
		multimap = MultimapBuilder.hashKeys().hashSetValues().build();
		targetAllList = new ArrayList<>();
	}

	/**
	 * Adds a {@link ToolInteractionRecipe} to this collection.
	 *
	 * @param recipe recipe to add
	 */
	public void add(@NotNull ToolInteractionRecipe recipe) {
		Set<Block> blocks = recipe.targetBlocks();
		if (blocks.isEmpty()) targetAllList.add(recipe);
		else for (Block block : blocks) multimap.put(block, recipe);
	}

	/**
	 * Attempts to perform one of the recipes stored in this collection.
	 *
	 * @param targetBlock target block
	 * @param context item usage context
	 * @return {@code true} if a recipe was performed, {@code false} otherwise.
	 */
	public boolean tryPerform(@NotNull Block targetBlock, @NotNull ItemUsageContext context) {
		for (ToolInteractionRecipe recipe : multimap.get(targetBlock)) {
			if (recipe.tryPerform(context)) return true;
		}
		for (ToolInteractionRecipe recipe : targetAllList) {
			if (recipe.tryPerform(context)) return true;
		}
		return false;
	}

	/**
	 * Clears this collection of all recipes.
	 */
	public void clear() {
		multimap.clear();
		targetAllList.clear();
	}
}
