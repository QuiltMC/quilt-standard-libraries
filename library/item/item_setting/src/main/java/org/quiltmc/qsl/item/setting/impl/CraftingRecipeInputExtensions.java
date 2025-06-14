package org.quiltmc.qsl.item.setting.impl;

import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.CraftingRecipe;

public interface CraftingRecipeInputExtensions {
	void quilt$setRecipe(CraftingRecipe recipe);

	void quilt$clearRecipe();

	@Nullable
	CraftingRecipe quilt$getRecipe();
}
