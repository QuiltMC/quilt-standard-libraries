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

import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandler;
import org.quiltmc.qsl.item.setting.mixin.SimpleInventoryMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Mixin(targets = {"net.minecraft.screen.LoomScreenHandler$C_ntobwfpp"})
public class LoomOutputSlotMixin extends Slot {
	public LoomOutputSlotMixin(Inventory inventory, int i, int j, int k) {
		super(inventory, i, j, k);
	}

	@Redirect(method= "onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;", ordinal = 1))
	public ItemStack getRecipeRemainder(Slot slot, int amount, PlayerEntity player, ItemStack stack) {
		ItemStack input = slot.takeStack(amount);

		RecipeRemainderLogicHandler.handleRemainderForPlayerCraft(
				input,
				null,
				((SimpleInventoryMixin) slot.inventory).getStacks(),
				slot.id,
				player);

		// Value not used, returning null is fine
		return null;
	}
}
