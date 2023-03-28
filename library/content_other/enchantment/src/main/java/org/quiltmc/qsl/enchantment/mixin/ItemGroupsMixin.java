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

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.HolderLookup;

import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
	@Redirect(method = "generateMaxEnchantedBookEntries", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
	private static Stream<Enchantment> filterCustomMaxEnchants(Stream<Enchantment> stream, Predicate<Enchantment> predicate, ItemGroup.ItemStackCollector collector, HolderLookup<Enchantment> enchantmentLookup, Set<EnchantmentTarget> targets, ItemGroup.Visibility visibility) {
		return stream.filter(predicate.or(enchantment -> enchantment instanceof QuiltEnchantment quiltEnchantment && quiltEnchantment.isVisible(visibility)));
	}

	@Redirect(method = "generateAllEnchantedBookEntries", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
	private static Stream<Enchantment> filterCustomAllEnchants(Stream<Enchantment> stream, Predicate<Enchantment> predicate, ItemGroup.ItemStackCollector collector, HolderLookup<Enchantment> enchantmentLookup, Set<EnchantmentTarget> targets, ItemGroup.Visibility visibility) {
		return stream.filter(predicate.or(enchantment -> enchantment instanceof QuiltEnchantment quiltEnchantment && quiltEnchantment.isVisible(visibility)));
	}
}
