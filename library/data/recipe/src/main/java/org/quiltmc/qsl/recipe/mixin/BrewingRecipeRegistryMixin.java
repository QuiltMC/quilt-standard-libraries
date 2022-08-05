package org.quiltmc.qsl.recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;

import org.quiltmc.qsl.recipe.impl.AbstractBrewingRecipe;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
	@Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
	private static void isValidIngredientForRecipes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (AbstractBrewingRecipe.VALID_INGREDIENTS.stream().anyMatch(ingredient -> ingredient.test(stack))) {
			cir.setReturnValue(true);
		}
	}
}
