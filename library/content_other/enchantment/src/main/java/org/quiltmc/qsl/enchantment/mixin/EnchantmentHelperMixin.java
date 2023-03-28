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

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.Weight;

import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.api.EnchantmentContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;

@Mixin(value = EnchantmentHelper.class)
public class EnchantmentHelperMixin {
	@Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;iterator()Ljava/util/Iterator;"))
	private static Iterator<Enchantment> removeCustomEnchants(Registry<Enchantment> registry) {
		return registry.stream().filter((enchantment) -> !(enchantment instanceof QuiltEnchantment)).iterator();
	}

	@Inject(method = "getPossibleEntries", at = @At("RETURN"), cancellable = true)
	private static void handleCustomEnchants(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		List<EnchantmentLevelEntry> entries = callback.getReturnValue();
		Registries.ENCHANTMENT.stream().filter((enchantment) -> enchantment instanceof QuiltEnchantment).forEach((enchantment) -> {
			for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
				EnchantmentContext context = EnchantmentGodClass.enchantmentContext.get().withLevel(level).withPower(power);
				int weight = ((QuiltEnchantment) enchantment).weightFromEnchantmentContext(context);
				if (weight > 0) {
					EnchantmentLevelEntry entry = new EnchantmentLevelEntry(enchantment, level);
					((WeightedAbsentAccessor) entry).setWeight(Weight.of(weight));
					entries.add(entry);
				}
			}
		});
		callback.setReturnValue(entries);
	}
}
