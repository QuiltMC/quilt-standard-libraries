/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.item.setting.api;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

/**
 * Quilt's version of {@link Item.Settings}.
 * Adds additional methods and hooks not found in the original class.
 * <p>
 * To use it, simply replace {@code new Item.Settings()} with {@code new QuiltItemSettings()}.
 */
public class QuiltItemSettings extends Item.Settings {
	/**
	 * Sets the {@link EquipmentSlotProvider} of the item.
	 *
	 * @param equipmentSlotProvider the {@link EquipmentSlotProvider}
	 * @return this
	 */
	public QuiltItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
		return this.customSetting(QuiltCustomItemSettings.EQUIPMENT_SLOT_PROVIDER, equipmentSlotProvider);
	}

	/**
	 * Sets the {@link EquipmentSlotProvider} of the item to always use {@code equipmentSlot}.
	 *
	 * @param equipmentSlot the {@link EquipmentSlot}
	 * @return this
	 */
	public QuiltItemSettings equipmentSlot(EquipmentSlot equipmentSlot) {
		return this.customSetting(QuiltCustomItemSettings.EQUIPMENT_SLOT_PROVIDER, itemStack -> equipmentSlot);
	}

	/**
	 * Sets the {@link CustomDamageHandler} of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
	 *
	 * @param handler the {@link CustomDamageHandler}
	 * @return this
	 * @see CustomDamageHandler
	 */
	public QuiltItemSettings customDamage(CustomDamageHandler handler) {
		return this.customSetting(QuiltCustomItemSettings.CUSTOM_DAMAGE_HANDLER, handler);
	}

	/**
	 * Sets a custom setting of the item.
	 *
	 * @param setting the unique type for this setting
	 * @param value   the object containing the setting itself
	 * @return this builder
	 */
	public <T> QuiltItemSettings customSetting(CustomItemSetting<T> setting, T value) {
		if (!(setting instanceof CustomItemSettingImpl)) {
			throw new UnsupportedOperationException("CustomItemSetting should not be custom class " + setting.getClass().getSimpleName());
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
