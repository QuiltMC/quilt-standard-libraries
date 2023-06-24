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

package org.quiltmc.qsl.item.extensions.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;
import org.quiltmc.qsl.item.extensions.api.crossbow.CrossbowExtensions;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
	// Make sure that the custom items are rendered in the correct place based on the current swing progress of the hand
	@Redirect(
			method = "getHandRenderType",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
	)
	private static boolean renderItem(ItemStack heldItem, Item item) {
		if (item == Items.BOW) {
			return heldItem.getItem() instanceof BowExtensions; // Return bow for rendering
		} else if (item == Items.CROSSBOW) {
			return heldItem.getItem() instanceof CrossbowExtensions; // Return crossbow for rendering
		}

		return heldItem.isOf(item); // Default behavior
	}

	@Redirect(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 1))
	private boolean renderFirstPersonItem(ItemStack heldItem, Item item) {
		if (heldItem.getItem() instanceof CrossbowExtensions) {
			return true; // Return true to invoke crossbow rendering path
		}

		return heldItem.isOf(item);
	}
}
