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

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@Mixin(targets = {"net.minecraft.screen.StonecutterScreenHandler$C_biccipxg"})
public class StonecutterOutputSlotMixin extends Slot {
	@Shadow
	@Dynamic
	StonecutterScreenHandler field_17639;

	public StonecutterOutputSlotMixin(Inventory inventory, int i, int j, int k) {
		super(inventory, i, j, k);
	}

	@Redirect(method = "onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;"))
	public ItemStack getRecipeRemainder(Slot slot, int amount, PlayerEntity player, ItemStack stack) {
		int selectedRecipe = this.field_17639.getSelectedRecipe();
		Recipe<?> recipe = selectedRecipe != -1 ? this.field_17639.getAvailableRecipes().get(selectedRecipe) : null;
		Item inputItem = slot.getStack().getItem();
		int inputCount = slot.getStack().getCount();

		RecipeRemainderLogicHandler.handleRemainderForScreenHandler(
				slot,
				amount,
				recipe,
				player
		);

		return new ItemStack(inputItem, Math.min(amount, inputCount));
	}
}
