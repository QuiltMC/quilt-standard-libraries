package org.quiltmc.qsl.item.setting.mixin.recipe_remainder;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderProvider;
import org.quiltmc.qsl.item.setting.impl.CraftingRecipeInputExtensions;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.CraftingRecipeInput;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingRecipe.class)
interface CraftingRecipeMixin extends Recipe<CraftingRecipeInput> {
	@WrapOperation(
		method = "getRecipeRemainders",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/recipe/CraftingRecipe;collectRecipeRemainders" +
				"(Lnet/minecraft/recipe/CraftingRecipeInput;)Lnet/minecraft/util/collection/DefaultedList;"
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
