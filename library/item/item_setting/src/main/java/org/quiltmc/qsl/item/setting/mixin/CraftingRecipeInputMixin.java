/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.item.setting.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.CraftingRecipeInput;

import org.quiltmc.qsl.item.setting.impl.CraftingRecipeInputExtensions;

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
