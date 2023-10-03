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

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.EnchantCommand;

import org.quiltmc.qsl.enchantment.api.QuiltEnchantableItem;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
	@Unique
	private static boolean quilt$execute$enchantableItemOverride = false;

	@Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"))
	private static boolean itemCanEnchantOverride(Enchantment enchantment, ItemStack stack) {
		if (stack.getItem() instanceof QuiltEnchantableItem enchantableItem) {
			return quilt$execute$enchantableItemOverride = enchantableItem.canEnchant(stack, enchantment);
		}

		quilt$execute$enchantableItemOverride = false;
		return enchantment.isAcceptableItem(stack);
	}

	@Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;isCompatible(Ljava/util/Collection;Lnet/minecraft/enchantment/Enchantment;)Z"))
	private static boolean itemCanEnchantOverride2(Collection<Enchantment> existing, Enchantment candidate) {
		return quilt$execute$enchantableItemOverride || EnchantmentHelper.isCompatible(existing, candidate);
	}
}
