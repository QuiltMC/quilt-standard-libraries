/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.enchantment.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

import org.quiltmc.qsl.enchantment.api.EnchantmentEvents;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantableItem;
import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.context.PlayerUsingBlockEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantmentHelper;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
	private int quilt$enchantLevel;

	public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/item/EnchantedBookItem;getEnchantmentNbt(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NbtList;"
					)
			),
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/enchantment/EnchantmentHelper;get(Lnet/minecraft/item/ItemStack;)Ljava/util/Map;",
					ordinal = 0
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void setAnvilContext(CallbackInfo ci, ItemStack ignored, int i, int j, int k, ItemStack input) {
		this.context.run((world, pos) -> QuiltEnchantmentHelper.setContext(new PlayerUsingBlockEnchantingContext(0, 0, input, world, world.getRandom(), true, this.player, pos)));
	}

	@ModifyVariable(
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
					),
					to = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"
					)
			),
			method = "updateResult",
			at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
			index = 14
	)
	private int captureEnchantLevel(int level) {
		return this.quilt$enchantLevel = level;
	}

	@Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"))
	private boolean checkWithContext(Enchantment enchantment, ItemStack stack) {
		EnchantingContext context = null;
		if (QuiltEnchantmentHelper.getContext() != null) {
			context = QuiltEnchantmentHelper.getContext().withLevel(this.quilt$enchantLevel);
		}

		if (!EnchantmentEvents.ANVIL_APPLICATION.invoker().canApply(enchantment, context)) {
			return false;
		}

		if (stack.getItem() instanceof QuiltEnchantableItem enchantableItem) {
			return enchantableItem.canEnchant(stack, enchantment);
		}

		if (enchantment instanceof QuiltEnchantment quiltEnchantment && context != null) {
			return quiltEnchantment.isAcceptableContext(context);
		}

		return enchantment.isAcceptableItem(stack);
	}

	@Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;canCombine(Lnet/minecraft/enchantment/Enchantment;)Z"))
	private boolean itemCanCombineOverride(Enchantment newEnchantment, Enchantment oldEnchantment) {
		ItemStack stack = this.input.getStack(0);
		if (stack.getItem() instanceof QuiltEnchantableItem enchantableItem) {
			return enchantableItem.canEnchant(stack, newEnchantment);
		}

		return newEnchantment.canCombine(oldEnchantment);
	}

	@Inject(method = "updateResult", at = @At("RETURN"))
	private void resetAnvilContext(CallbackInfo ci) {
		QuiltEnchantmentHelper.clearContext();
		this.quilt$enchantLevel = 0;
	}
}
