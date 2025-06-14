package org.quiltmc.qsl.item.setting.mixin;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.setting.impl.CraftingRecipeInputExtensions;

import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.CraftingRecipeInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Allows attaching a recipe to the input as a way of adding a recipe param to
 * {@link CraftingRecipe#collectRecipeRemainders(CraftingRecipeInput)}.<br>
 * Usually a field would be added to {@link CraftingRecipe} instead, but it's an interface so that doesn't work
 * without adding the field to each implementation.
 */
@Mixin(CraftingRecipeInput.class)
abstract class CraftingRecipeInputMixin implements CraftingRecipeInputExtensions {
	@Unique
	@Nullable
	private CraftingRecipe recipe;

	@Override
	public void quilt$setRecipe(CraftingRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void quilt$clearRecipe() {
		this.recipe = null;
	}

	@Override
	@Nullable
	public CraftingRecipe quilt$getRecipe() {
		return this.recipe;
	}
}
