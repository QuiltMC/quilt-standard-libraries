/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.item.setting.api;

import java.util.Map;

import org.jetbrains.annotations.Contract;

import net.minecraft.component.DataComponentType;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.unmapped.C_yeazlrrn;
import net.minecraft.util.Rarity;

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

/**
 * Quilt's version of {@link Item.Settings}.
 * Adds additional methods and hooks not found in the original class.
 *
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
	 * Sets the stack-aware recipe remainder provider of the item.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 */
	public QuiltItemSettings recipeRemainder(RecipeRemainderProvider provider) {
		return this.recipeRemainder(provider, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	public QuiltItemSettings recipeDamageRemainder() {
		return this.recipeDamageRemainder(1, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	public QuiltItemSettings recipeSelfRemainder() {
		return this.recipeDamageRemainder(0, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param by the amount
	 */
	public QuiltItemSettings recipeDamageRemainder(int by) {
		return this.recipeDamageRemainder(by, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	public QuiltItemSettings recipeRemainder(RecipeRemainderProvider provider, RecipeRemainderLocation... locations) {
		for (var location : locations) {
			((CustomItemSettingImpl<Map<RecipeRemainderLocation, RecipeRemainderProvider>>) QuiltCustomItemSettings.RECIPE_REMAINDER_PROVIDER)
					.get(this)
					.put(location, provider);
		}

		return this;
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	public QuiltItemSettings recipeDamageRemainder(RecipeRemainderLocation... locations) {
		return this.recipeDamageRemainder(1, locations);
	}

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	public QuiltItemSettings recipeSelfRemainder(RecipeRemainderLocation... locations) {
		return this.recipeDamageRemainder(0, locations);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 *
	 * @param by       the amount
	 * @param locations the {@link RecipeRemainderLocation location} for the remainder
	 */
	public QuiltItemSettings recipeDamageRemainder(int by, RecipeRemainderLocation... locations) {
		if (by == 0) {
			return this.recipeRemainder((original, recipe) -> original.copy(), locations);
		}

		return this.recipeRemainder((original, recipe) -> {
			if (!original.isDamageable()) {
				return original.copy();
			}

			ItemStack copy = original.copy();

			copy.setDamage(copy.getDamage() + by);

			if (copy.getDamage() >= copy.getMaxDamage()) {
				copy.setCount(0);
				return ItemStack.EMPTY;
			}

			return copy;
		}, locations);
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
	@Contract("_->this")
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
	@Contract("_->this")
	public QuiltItemSettings maxDamage(int maxDamage) {
		super.maxDamage(maxDamage);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltItemSettings recipeRemainder(Item recipeRemainder) {
		super.recipeRemainder(recipeRemainder);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltItemSettings rarity(Rarity rarity) {
		super.rarity(rarity);
		return this;
	}

	@Override
	@Contract("->this")
	public QuiltItemSettings fireproof() {
		super.fireproof();
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltItemSettings requiredFlags(FeatureFlag... flags) {
		super.requiredFlags(flags);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltItemSettings jukeboxSong(RegistryKey<C_yeazlrrn> song) {
		super.jukeboxSong(song);
		return this;
	}

	@Override
	@Contract("_,_->this")
	public <T> QuiltItemSettings component(DataComponentType<T> type, T value) {
		super.component(type, value);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltItemSettings attributeModifiersComponent(AttributeModifiersComponent value) {
		super.attributeModifiersComponent(value);
		return this;
	}
}
