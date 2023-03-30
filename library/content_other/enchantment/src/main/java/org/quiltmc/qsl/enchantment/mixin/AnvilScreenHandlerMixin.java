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

import org.quiltmc.qsl.enchantment.api.PlayerUsingBlockEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;

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
		this.context.run((world, pos) -> EnchantmentGodClass.context.set(new PlayerUsingBlockEnchantingContext(0, 0, input, world, world.getRandom(), true, this.player, pos)));
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
		if (enchantment instanceof QuiltEnchantment quiltEnchantment && EnchantmentGodClass.context.get() != null) {
			return quiltEnchantment.isAcceptableContext(EnchantmentGodClass.context.get().withLevel(this.quilt$enchantLevel));
		} else {
			// For clients, we always return false. The server sets the stack anyway, so it doesn't affect anything
			return false;
		}
	}

	@Inject(method = "updateResult", at = @At("RETURN"))
	private void resetAnvilContext(CallbackInfo ci) {
		EnchantmentGodClass.context.remove();
	}
}
