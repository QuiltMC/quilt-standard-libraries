/*
 * Copyright 2022 The Quilt Project
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

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.recipe.RecipeHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@Mixin(targets = {"net/minecraft/screen/StonecutterScreenHandler$C_biccipxg"})
abstract class StonecutterOutputSlotMixin extends Slot {
	@Shadow
	@Dynamic
	StonecutterScreenHandler field_17639;

	private StonecutterOutputSlotMixin() {
		super(null, 0, 0, 0);
		throw new AssertionError("dummy constructor called");
	}

	// MCDev erroneously says this method and target are incorrect; the anonymous class is probably confusing it
	@Redirect(
			method = "onTakeItem",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/screen/slot/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;"
			)
	)
	public ItemStack getRecipeRemainder(Slot slot, int amount, PlayerEntity player, ItemStack stack) {
		Recipe<?> recipe = this.getRecipe();
		ItemStack inputStack = slot.getStack();
		Item inputItem = inputStack.getItem();
		int inputCount = inputStack.getCount();

		RecipeRemainderLogicHandler.handleRemainderForScreenHandler(
				slot,
				amount,
				recipe,
				RecipeRemainderLocation.STONECUTTER_INPUT,
				player
		);

		return new ItemStack(inputItem, Math.min(amount, inputCount));
	}

	@Unique
	private @Nullable Recipe<?> getRecipe() {
		int selectedRecipe = this.field_17639.getSelectedRecipe();

		if (selectedRecipe == -1) {
			return null;
		}

		Optional<RecipeHolder<StonecuttingRecipe>> recipe = this.field_17639
				.method_17863()
				.entries()
				.get(selectedRecipe)
				.recipe()
				.recipe();

		return recipe.map(RecipeHolder::value).orElse(null);
	}
}
