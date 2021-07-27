/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.item.api.item.v1;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;

import org.quiltmc.qsl.item.impl.QuiltItemInternals;

/**
 * Quilt's version of {@link Item.Settings}. Adds additional methods and hooks
 * not found in the original class.
 *
 * <p>To use it, simply replace {@code new Item.Settings()} with
 * {@code new QuiltItemSettings()}.
 */
public class QuiltItemSettings extends Item.Settings {
	/**
	 * Sets the {@link EquipmentSlotProvider} of the item.
	 *
	 * @param equipmentSlotProvider The {@link EquipmentSlotProvider}
	 * @return this
	 */
	public QuiltItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
		QuiltItemInternals.computeExtraData(this).equipmentSlot(equipmentSlotProvider);
		return this;
	}

	/**
	 * Sets the {@link CustomDamageHandler} of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
	 * @see CustomDamageHandler
	 * @param handler The {@link CustomDamageHandler}
	 * @return this
	 */
	public QuiltItemSettings customDamage(CustomDamageHandler handler) {
		QuiltItemInternals.computeExtraData(this).customDamage(handler);
		return this;
	}

	// Overrides of vanilla methods

	@Override
	public QuiltItemSettings food(FoodComponent foodComponent) {
		super.food(foodComponent);
		return this;
	}

	@Override
	public QuiltItemSettings maxCount(int maxCount) {
		super.maxCount(maxCount);
		return this;
	}

	@Override
	public QuiltItemSettings maxDamageIfAbsent(int maxDamage) {
		super.maxDamageIfAbsent(maxDamage);
		return this;
	}

	@Override
	public QuiltItemSettings maxDamage(int maxDamage) {
		super.maxDamage(maxDamage);
		return this;
	}

	@Override
	public QuiltItemSettings recipeRemainder(Item recipeRemainder) {
		super.recipeRemainder(recipeRemainder);
		return this;
	}

	@Override
	public QuiltItemSettings group(ItemGroup group) {
		super.group(group);
		return this;
	}

	@Override
	public QuiltItemSettings rarity(Rarity rarity) {
		super.rarity(rarity);
		return this;
	}

	@Override
	public QuiltItemSettings fireproof() {
		super.fireproof();
		return this;
	}
}
