/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.item.content.registry.mixin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;

import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.content.registry.api.ItemContentRegistries;

@Mixin(FuelTimes.class)
abstract class FuelTimesMixin {
	// Mixins are redirects here because we don't want to silently error if another mod is incompatible here

	@Redirect(method = "isFuel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;containsKey(Ljava/lang/Object;)Z", remap = false))
	private boolean isFuelWithREA(Object2IntSortedMap<Item> instance, Object o, @Local(argsOnly = true) ItemStack stack) {
		return ItemContentRegistries.FUEL_TIMES.get(stack.getItem()).isPresent();
	}

	@Redirect(
			method = "validItems",
			at = @At(
				value = "INVOKE",
				target = "Ljava/util/Collections;unmodifiableSequencedSet(Ljava/util/SequencedSet;)"
					+ "Ljava/util/SequencedSet;",
				remap = false
			)
	)
	private SequencedSet<Item> validFuelsWithREA(SequencedSet<? extends Item> instance) {
		SequencedSet<Item> items = new LinkedHashSet<>();
		ItemContentRegistries.FUEL_TIMES.forEach((entry) -> items.add(entry.entry()));

		return Collections.unmodifiableSequencedSet(items);
	}

	@Redirect(
			method = "getFuelTime",
			at = @At(
				value = "INVOKE",
				target = "Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;getInt(Ljava/lang/Object;)I",
				remap = false
			)
	)
	public int getFuelTimeWithREA(
			Object2IntSortedMap<Item> instance, Object o, @Local(argsOnly = true) ItemStack stack
	) {
		return ItemContentRegistries.FUEL_TIMES.get(stack.getItem()).orElse(0);
	}
}

