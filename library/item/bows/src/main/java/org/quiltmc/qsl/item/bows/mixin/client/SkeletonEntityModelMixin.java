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

package org.quiltmc.qsl.item.bows.mixin.client;

import org.quiltmc.qsl.item.bows.api.BowExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(SkeletonEntityModel.class)
public abstract class SkeletonEntityModelMixin {
	// Allows Skeletons to visually shoot custom bows by returning true
	@Redirect(method = "animateModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
	private boolean animateModel(ItemStack heldItemStack, Item item) {
		return heldItemStack.getItem() instanceof BowExtensions || heldItemStack.getItem() == Items.BOW;
	}

	// Allows Skeletons to visually shoot custom bows by returning true
	@Redirect(method = "setAngles", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
	private boolean setAngles(ItemStack heldItemStack, Item item) {
		return heldItemStack.getItem() instanceof BowExtensions || heldItemStack.getItem() == Items.BOW;
	}
}
