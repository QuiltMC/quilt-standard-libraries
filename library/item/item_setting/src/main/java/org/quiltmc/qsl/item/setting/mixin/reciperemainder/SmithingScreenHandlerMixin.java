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

package org.quiltmc.qsl.item.setting.mixin.reciperemainder;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandler;
import org.quiltmc.qsl.item.setting.mixin.SimpleInventoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {
	@Shadow
	private @Nullable SmithingRecipe currentRecipe;

	@Unique
	private SmithingRecipe pastRecipe;

	@Shadow
	public abstract void updateResult();

	public SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
		super(screenHandlerType, i, playerInventory, screenHandlerContext);
	}

	@Redirect(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/SmithingScreenHandler;decrementStack(I)V"))
	private void applyRecipeRemainder(SmithingScreenHandler instance, int slot, PlayerEntity player, ItemStack stack) {
		ItemStack inputStack = this.input.getStack(slot);
		ItemStack remainder = RecipeRemainderLogicHandler.getRemainder(
				inputStack,
				this.currentRecipe == null ? this.pastRecipe : this.currentRecipe
		);

		inputStack.decrement(1);

		RecipeRemainderLogicHandler.handleRemainderForPlayerCraft(
				remainder,
				((SimpleInventoryAccessor) this.input).getStacks(),
				slot,
				player
		);

		this.pastRecipe = this.currentRecipe;
	}

	@Inject(method = "onTakeOutput", at = @At("RETURN"))
	public void refreshOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		this.updateResult();
	}
}
