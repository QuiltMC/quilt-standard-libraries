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

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;

import org.quiltmc.qsl.enchantment.api.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantmentHelper;

@Mixin(EnchantRandomlyLootFunction.class)
public class EnchantRandomlyLootFunctionMixin {
	@Inject(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;stream()Ljava/util/stream/Stream;"))
	private void setEnchantingLootContext(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		QuiltEnchantmentHelper.setContext(new EnchantingContext(0, 0, stack, context.getWorld(), context.getRandom(), true));
	}

	@Redirect(method = "process", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 1))
	private Stream<Enchantment> filterWithContext(Stream<Enchantment> stream, Predicate<Enchantment> predicate) {
		return stream.filter(enchantment -> {
			if (enchantment instanceof QuiltEnchantment quiltEnchantment) {
				return quiltEnchantment.isAcceptableContext(QuiltEnchantmentHelper.getContext());
			}

			return predicate.test(enchantment);
		});
	}

	@Inject(method = "process", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
	private void removeEnchantingLootContext(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		QuiltEnchantmentHelper.clearContext();
	}
}
