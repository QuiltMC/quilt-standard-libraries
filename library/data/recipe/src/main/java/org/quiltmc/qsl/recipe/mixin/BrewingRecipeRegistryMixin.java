/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.recipe.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;

import org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe;
import org.quiltmc.qsl.recipe.api.brewing.PotionBrewingRecipe;
import org.quiltmc.qsl.recipe.impl.RecipeImpl;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
	@Shadow
	@Final
	private static List<Ingredient> POTION_TYPES;

	@Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
	private static void isValidIngredientForRecipes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (AbstractBrewingRecipe.VALID_INGREDIENTS.stream().anyMatch(ingredient -> ingredient.test(stack))) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "registerDefaults", at = @At("HEAD"))
	private static void registerPotionTypesTag(CallbackInfo ci) {
		POTION_TYPES.add(Ingredient.ofTag(RecipeImpl.VALID_INPUTS));
	}

	@Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
	private static void isBrewableFromRecipe(Potion potion, CallbackInfoReturnable<Boolean> cir) {
		if (PotionBrewingRecipe.BREWABLE_POTIONS.contains(potion)) {
			cir.setReturnValue(true);
		}
	}
}
