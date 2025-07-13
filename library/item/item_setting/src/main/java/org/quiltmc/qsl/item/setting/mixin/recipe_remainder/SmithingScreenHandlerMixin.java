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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.recipe.Recipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@Mixin(SmithingScreenHandler.class)
abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {
	@Shadow
	public abstract void updateResult();

	@SuppressWarnings("DataFlowIssue")
	private SmithingScreenHandlerMixin() {
		super(null, 0, null, null, null);
		throw new AssertionError("dummy constructor called");
	}

	// save the last recipe because unlockLastRecipe clears it
	@Inject(
			method = "onTakeOutput",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/inventory/CraftingResultInventory;unlockLastRecipe"
					+ "(Lnet/minecraft/entity/player/PlayerEntity;Ljava/util/List;)V"
			)
	)
	private void shareLastRecipe(
			CallbackInfo ci,
			@Share("lastRecipe") LocalRef<@Nullable Recipe<?>> lastRecipe
	) {
		RecipeHolder<?> lastRecipeHolder = this.result.getLastRecipe();
		lastRecipe.set(lastRecipeHolder == null ? null : lastRecipeHolder.value());
	}

	@Redirect(
			method = "onTakeOutput",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/SmithingScreenHandler;decrementStack(I)V")
	)
	private void applyRecipeRemainderToIngredient(
			SmithingScreenHandler instance, int slot,
			@Share("lastRecipe") LocalRef<@Nullable Recipe<?>> lastRecipe
	) {
		RecipeRemainderLogicHandler.handleRemainderForScreenHandler(
				this.getSlot(slot),
				1,
				lastRecipe.get(),
				switch (slot) {
					case SmithingScreenHandler.TEMPLATE_SLOT -> RecipeRemainderLocation.SMITHING_TEMPLATE;
					case SmithingScreenHandler.BASE_SLOT -> RecipeRemainderLocation.SMITHING_BASE;
					case SmithingScreenHandler.ADDITIONAL_SLOT -> RecipeRemainderLocation.SMITHING_INGREDIENT;
					default -> throw new IllegalStateException("Unexpected value: " + slot);
				},
				this.player
		);
	}

	@Inject(method = "onTakeOutput", at = @At("RETURN"))
	private void refreshOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		this.updateResult();
	}
}
