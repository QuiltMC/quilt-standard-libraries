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
import org.quiltmc.qsl.item.impl.CustomItemSettingImpl;

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
		return this.customSetting(CustomItemSettingImpl.EQUIPMENT_SLOT_PROVIDER, equipmentSlotProvider);
	}

	/**
	 * Sets the {@link CustomDamageHandler} of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
	 * @see CustomDamageHandler
	 * @param handler The {@link CustomDamageHandler}
	 * @return this
	 */
	public QuiltItemSettings customDamage(CustomDamageHandler handler) {
		return this.customSetting(CustomItemSettingImpl.CUSTOM_DAMAGE_HANDLER, handler);
	}

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 */
	public QuiltItemSettings recipeRemainder(RecipeRemainderProvider provider) {
		return this.customSetting(CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER, provider);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 */
	public QuiltItemSettings damageIfUsedInCrafting() {
		return this.damageIfUsedInCrafting(1);
	}

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 */
	public QuiltItemSettings returnSelfInCrafting() {
		return this.damageIfUsedInCrafting(0);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 *
	 * @param by the amount
	 */
	public QuiltItemSettings damageIfUsedInCrafting(int by) {
		if (by == 0) {
			return this.recipeRemainder((original, inventory, type, world, pos) -> original);
		}

		return this.recipeRemainder((original, inventory, type, world, pos) -> {
			if (!original.isDamageable()) {
				return ItemStack.EMPTY;
			}

			ItemStack copy = original.copy();

			if (copy.damage(by, world.random, null)) {
				return ItemStack.EMPTY;
			}

			return copy;
		});
	}

	/**
	 * Sets a custom setting of the item.
	 * @param setting the unique type for this setting
	 * @param value the object containing the setting itself
	 * @return this builder
	 */
	public <T> QuiltItemSettings customSetting(CustomItemSetting<T> setting, T value) {
		if (!(setting instanceof CustomItemSettingImpl)) {
			throw new UnsupportedOperationException("CustomItemSetting should not be ");
		}

		((CustomItemSettingImpl<T>) setting).set(this, value);
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
