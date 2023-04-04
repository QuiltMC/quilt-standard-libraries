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

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.random.RandomGenerator;

import org.quiltmc.qsl.enchantment.api.context.PlayerUsingBlockEnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantmentHelper;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {
	@Shadow
	@Final
	private ScreenHandlerContext context;
	@Shadow
	@Final
	private RandomGenerator random;
	@Unique
	private PlayerEntity quilt$player;

	protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
	public void capturePlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo callback) {
		this.quilt$player = playerInventory.player;
	}

	@Inject(method = "generateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Lnet/minecraft/util/random/RandomGenerator;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;"))
	private void setEnchantmentContext(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		// Level and power will be set later when those values are figured out.
		this.context.run((world, pos) -> QuiltEnchantmentHelper.setContext(new PlayerUsingBlockEnchantingContext(0, 0, stack, world, this.random, false, this.quilt$player, pos)));
	}

	@Inject(method = "generateEnchantments", at = @At("RETURN"))
	private void clearEnchantmentContext(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		QuiltEnchantmentHelper.clearContext();
	}
}
