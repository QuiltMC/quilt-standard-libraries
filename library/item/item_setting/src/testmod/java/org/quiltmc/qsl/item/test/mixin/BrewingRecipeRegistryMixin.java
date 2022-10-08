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

package org.quiltmc.qsl.item.test.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;

import org.quiltmc.qsl.item.test.RecipeRemainderTests;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
	@Shadow
	private static void registerPotionRecipe(Potion input, Item item, Potion output) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Inject(method = "registerDefaults", at = @At("TAIL"))
	private static void registerRemainder(CallbackInfo ci) {
		registerPotionRecipe(Potions.WATER, RecipeRemainderTests.POTION_INGREDIENT_REMAINDER, Potions.FIRE_RESISTANCE);
	}
}
