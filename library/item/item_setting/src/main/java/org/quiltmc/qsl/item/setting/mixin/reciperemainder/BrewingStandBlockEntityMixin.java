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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.setting.impl.RecipeRemainderLogicHandler;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@Shadow
	@Final
	private static int INGREDIENT_SLOT;

	@Inject(method = "craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"),
			cancellable = true
	)
	private static void applyRecipeRemainder(World world, BlockPos pos, DefaultedList<ItemStack> inventory, CallbackInfo ci) {
		ItemStack ingredient = inventory.get(INGREDIENT_SLOT);

		ItemStack remainder = RecipeRemainderLogicHandler.getRemainder(ingredient, null);

		if (!remainder.isEmpty()) {
			ingredient.decrement(1);

			RecipeRemainderLogicHandler.handleRemainderForNonPlayerCraft(
					remainder,
					inventory,
					INGREDIENT_SLOT,
					world,
					pos
			);

			world.syncWorldEvent(1035, pos, 0);
			ci.cancel();
		}
	}
}
