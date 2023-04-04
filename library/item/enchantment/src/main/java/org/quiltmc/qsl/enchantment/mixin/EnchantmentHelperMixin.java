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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.random.RandomGenerator;

import org.quiltmc.qsl.enchantment.api.QuiltEnchantableItem;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.api.context.EnchantingContext;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantmentHelper;

@Mixin(value = EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@Inject(method = "generateEnchantments", at = @At("HEAD"))
	private static void addRandomContext(RandomGenerator random, ItemStack stack, int experienceLevel, boolean treasureAllowed, CallbackInfoReturnable<ItemStack> cir) {
		if (QuiltEnchantmentHelper.getContext() != null) {
			QuiltEnchantmentHelper.setContext(QuiltEnchantmentHelper.getContext().withCoreContext(stack, random, treasureAllowed));
		}
	}

	@Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;iterator()Ljava/util/Iterator;"))
	private static Iterator<Enchantment> removeCustomEnchants(Registry<Enchantment> registry, int power, ItemStack stack) {
		return registry.stream()
			.filter((enchantment) -> !(enchantment instanceof QuiltEnchantment))
			.filter(enchantment -> quilt$checkItemEnchantOverride(stack, enchantment))
			.iterator();
	}

	@Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"))
	private static boolean itemCanAcceptOverride(EnchantmentTarget type, Item item, int power, ItemStack stack) {
		// From the iterator filtering, we know that if the item is a QuiltEnchantableItem, it accepts the enchantment.
		return item instanceof QuiltEnchantableItem || type.isAcceptableItem(item);
	}

	@Inject(method = "getPossibleEntries", at = @At("RETURN"), cancellable = true)
	private static void handleCustomEnchants(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		List<EnchantmentLevelEntry> entries = callback.getReturnValue();
		Registries.ENCHANTMENT.stream().filter((enchantment) -> enchantment instanceof QuiltEnchantment && enchantment.isAvailableForRandomSelection()).forEach((enchantment) -> {
			for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); --level) {
				boolean validEntry = false;
				EnchantmentLevelEntry entry = new EnchantmentLevelEntry(enchantment, level);
				if (quilt$checkItemEnchantOverride(stack, enchantment)) {
					if (QuiltEnchantmentHelper.getContext() != null) {
						EnchantingContext context = QuiltEnchantmentHelper.getContext().withLevel(level).withPower(power);

						int weight = 0;
						if (((QuiltEnchantment) enchantment).isAcceptableContext(context)) {
							weight = ((QuiltEnchantment) enchantment).weightFromContext(context);
						}

						((WeightedAbsentAccessor) entry).setWeight(Weight.of(weight));
						validEntry = weight > 0;
					} else if ((!enchantment.isTreasure() || treasureAllowed) && (enchantment.isAcceptableItem(stack) || stack.isOf(Items.BOOK))) {
						validEntry = true;
					}
				}

				if (validEntry) {
					entries.add(entry);
					break;
				}
			}
		});
		callback.setReturnValue(entries);
	}

	@Redirect(method = "removeConflicts", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;canCombine(Lnet/minecraft/enchantment/Enchantment;)Z"))
	private static boolean removeConflictsWithItemOverride(Enchantment pickedEnchantment, Enchantment possibleEnchantment) {
		if (QuiltEnchantmentHelper.getContext() != null) {
			ItemStack stack = QuiltEnchantmentHelper.getContext().getStack();
			if (stack.getItem() instanceof QuiltEnchantableItem enchantableItem) {
				return enchantableItem.canEnchant(stack, possibleEnchantment);
			}
		}

		return pickedEnchantment.canCombine(possibleEnchantment);
	}

	/**
	 * @return {@code true} if the item can be enchanted or the item is not a {@link QuiltEnchantableItem}, or {@code false} otherwise
	 */
	@Unique
	private static boolean quilt$checkItemEnchantOverride(ItemStack stack, Enchantment enchantment) {
		if (!(stack.getItem() instanceof QuiltEnchantableItem enchantableItem)) {
			return true;
		}

		return enchantableItem.canEnchant(stack, enchantment);
	}
}
