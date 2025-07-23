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

package org.quiltmc.qsl.item.setting.mixin.recipe_remainder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.CraftingRecipeInput;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderProvider;
import org.quiltmc.qsl.item.setting.impl.CraftingRecipeInputExtensions;

@Mixin(CraftingRecipe.class)
interface CraftingRecipeMixin extends Recipe<CraftingRecipeInput> {
	@WrapOperation(
			method = "getRecipeRemainders",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/recipe/CraftingRecipe;collectRecipeRemainders"
					+ "(Lnet/minecraft/recipe/CraftingRecipeInput;)Lnet/minecraft/util/collection/DefaultedList;"
			)
	)
	private DefaultedList<ItemStack> passRecipeWithInput(
			CraftingRecipeInput input, Operation<DefaultedList<ItemStack>> original
	) {
		final CraftingRecipeInputExtensions extendedInput = (CraftingRecipeInputExtensions) input;
		try {
			// attach recipe to input
			extendedInput.quilt$setRecipe((CraftingRecipe) this);
			// MCDev erroneously assert this call has incorrect parameters
			return original.call(input);
		} finally {
			extendedInput.quilt$clearRecipe();
		}
	}

	@ModifyReturnValue(method = "collectRecipeRemainders", at = @At(value = "RETURN"))
	private static DefaultedList<ItemStack> modifyRemainder(
			DefaultedList<ItemStack> original, CraftingRecipeInput recipeInput
	) {
		RecipeRemainderProvider.getRemainingStacks(
				recipeInput,
				// retrieve recipe from input
				((CraftingRecipeInputExtensions) recipeInput).quilt$getRecipe(),
				RecipeRemainderLocation.CRAFTING, original
		);

		return original;
	}
}
