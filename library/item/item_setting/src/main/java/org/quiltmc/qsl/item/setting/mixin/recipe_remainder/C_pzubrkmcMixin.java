/*
 * Copyright 2022-2023 QuiltMC
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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.unmapped.C_pzubrkmc;
import net.minecraft.unmapped.C_xrtznmeb;

import org.quiltmc.qsl.item.setting.api.RecipeRemainderLogicHandler;

@SuppressWarnings({"deprecated", "removal"})
@Mixin(C_pzubrkmc.class)
public abstract class C_pzubrkmcMixin extends ForgingScreenHandler {
	@Shadow
	private @Nullable C_xrtznmeb f_xqlemijg;

	@Shadow
	public abstract void updateResult();

	public C_pzubrkmcMixin(@Nullable ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
		super(screenHandlerType, i, playerInventory, screenHandlerContext);
	}

	@Redirect(method = "m_yukwcfqb", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private void applyRecipeRemainder(ItemStack instance, int amount, int slot) {
		RecipeRemainderLogicHandler.handleRemainderForScreenHandler(
				this.getSlot(slot),
				amount,
				this.f_xqlemijg,
				this.player
		);
	}

	@Inject(method = "onTakeOutput", at = @At("RETURN"))
	public void refreshOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		this.updateResult();
	}
}
