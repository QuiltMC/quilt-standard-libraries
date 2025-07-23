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

package org.quiltmc.qsl.item.setting.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.setting.api.CustomDamageHandler;
import org.quiltmc.qsl.item.setting.api.CustomItemSetting;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettingsExtensions;
import org.quiltmc.qsl.item.setting.api.QuiltCustomItemSettings;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderProvider;
import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

// for injected interface methods
@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Item.Settings.class)
abstract class ItemSettingsMixin implements QuiltItemSettingsExtensions {
	/**
	 * Sets the {@link CustomDamageHandler} of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
	 *
	 * @param handler the {@link CustomDamageHandler}
	 * @return this
	 * @see CustomDamageHandler
	 */
	@Override
	public Item.Settings customDamage(CustomDamageHandler handler) {
		return this.customSetting(QuiltCustomItemSettings.CUSTOM_DAMAGE_HANDLER, handler);
	}

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 */
	@Override
	public Item.Settings recipeRemainder(RecipeRemainderProvider provider) {
		return this.recipeRemainder(provider, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	@Override
	public Item.Settings recipeDamageRemainder() {
		return this.recipeDamageRemainder(1, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	@Override
	public Item.Settings recipeSelfRemainder() {
		return this.recipeDamageRemainder(0, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param by the amount
	 */
	@Override
	public Item.Settings recipeDamageRemainder(int by) {
		return this.recipeDamageRemainder(by, RecipeRemainderLocation.DEFAULT_LOCATIONS);
	}

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	@Override
	public Item.Settings recipeRemainder(RecipeRemainderProvider provider, RecipeRemainderLocation... locations) {
		for (final RecipeRemainderLocation location : locations) {
			(
				(CustomItemSettingImpl<Map<RecipeRemainderLocation, RecipeRemainderProvider>>)
					QuiltCustomItemSettings.RECIPE_REMAINDER_PROVIDER
			)
					.get((Item.Settings) (Object) this)
					.put(location, provider);
		}

		return (Item.Settings) (Object) this;
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	@Override
	public Item.Settings recipeDamageRemainder(RecipeRemainderLocation... locations) {
		return this.recipeDamageRemainder(1, locations);
	}

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	@Override
	public Item.Settings recipeSelfRemainder(RecipeRemainderLocation... locations) {
		return this.recipeDamageRemainder(0, locations);
	}

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 *
	 * @param by       the amount
	 * @param locations the {@link RecipeRemainderLocation location} for the remainder
	 */
	@Override
	public Item.Settings recipeDamageRemainder(int by, RecipeRemainderLocation... locations) {
		if (by == 0) {
			return this.recipeRemainder(
				(original, recipe) -> {
					final ItemStack copy = original.copy();
					copy.setCount(1);
					return copy;
				},
				locations
			);
		}

		return this.recipeRemainder(
			(original, recipe) -> {
				final ItemStack copy = original.copy();
				copy.setCount(1);

				if (!original.isDamageable()) {
					return copy;
				}

				copy.setDamage(copy.getDamage() + by);

				if (copy.getDamage() >= copy.getMaxDamage()) {
					copy.setCount(0);
					return ItemStack.EMPTY;
				}

				return copy;
			},
			locations
		);
	}

	/**
	 * Sets a custom setting of the item.
	 *
	 * @param setting the unique type for this setting
	 * @param value   the object containing the setting itself
	 * @return this builder
	 */
	@Override
	public <T> Item.Settings customSetting(CustomItemSetting<T> setting, T value) {
		if (!(setting instanceof CustomItemSettingImpl)) {
			throw new UnsupportedOperationException(
				"CustomItemSetting should not be custom class " + setting.getClass().getSimpleName()
			);
		}

		final Item.Settings thisCast = (Item.Settings) (Object) this;
		((CustomItemSettingImpl<T>) setting).set(thisCast, value);
		return thisCast;
	}
}
