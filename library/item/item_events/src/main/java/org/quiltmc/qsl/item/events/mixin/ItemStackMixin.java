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

package org.quiltmc.qsl.item.events.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import org.quiltmc.qsl.item.events.api.ItemInteractionEvents;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
	private ActionResult quilt$invokeUsedOnBlockEvent(Item instance, ItemUsageContext context) {
		var result = ItemInteractionEvents.USED_ON_BLOCK.invoker().onItemUsedOnBlock(context);
		if (result == ActionResult.PASS) {
			result = instance.useOnBlock(context);
		}
		return result;
	}

	@Redirect(method = "useOnEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
	private ActionResult quilt$invokeUsedOnEntityEvent(Item instance, ItemStack stack, PlayerEntity user,
			LivingEntity entity, Hand hand) {
		var result = ItemInteractionEvents.USED_ON_ENTITY.invoker().onItemUsedOnEntity(stack, user, entity, hand);
		if (result == ActionResult.PASS) {
			result = instance.useOnEntity(stack, user, entity, hand);
		}
		return result;
	}
}
