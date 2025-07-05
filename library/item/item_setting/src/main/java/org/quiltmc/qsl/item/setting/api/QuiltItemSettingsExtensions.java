package org.quiltmc.qsl.item.setting.api;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Interface implemented by {@link Item.Settings} instances when QSL is present.
 */
@InjectedInterface(Item.Settings.class)
public interface QuiltItemSettingsExtensions {
	/**
	 * Sets the {@link CustomDamageHandler} of the item.
	 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
	 *
	 * @param handler the {@link CustomDamageHandler}
	 * @return this
	 * @see CustomDamageHandler
	 */
	Item.Settings customDamage(CustomDamageHandler handler);

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 */
	Item.Settings recipeRemainder(RecipeRemainderProvider provider);

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	Item.Settings recipeDamageRemainder();

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 */
	Item.Settings recipeSelfRemainder();

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 * Defaults to setting both crafting, furnace fuel remainder, and brewing stand addition, like vanilla.
	 *
	 * @param by the amount
	 */
	Item.Settings recipeDamageRemainder(int by);

	/**
	 * Sets the stack-aware recipe remainder provider of the item.
	 *
	 * @param provider the {@link RecipeRemainderProvider} for the item
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	Item.Settings recipeRemainder(RecipeRemainderProvider provider, RecipeRemainderLocation... locations);

	/**
	 * Sets the stack-aware recipe remainder to damage the item by 1 every time it is used in crafting.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	Item.Settings recipeDamageRemainder(RecipeRemainderLocation... locations);

	/**
	 * Sets the stack-aware recipe remainder to return the item itself.
	 *
	 * @param locations the {@link RecipeRemainderLocation locations} for the remainder
	 */
	Item.Settings recipeSelfRemainder(RecipeRemainderLocation... locations);

	/**
	 * Sets the stack-aware recipe remainder to damage the item by a certain amount every time it is used in crafting.
	 *
	 * @param by       the amount
	 * @param locations the {@link RecipeRemainderLocation location} for the remainder
	 */
	Item.Settings recipeDamageRemainder(int by, RecipeRemainderLocation... locations);

	/**
	 * Sets a custom setting of the item.
	 *
	 * @param setting the unique type for this setting
	 * @param value   the object containing the setting itself
	 * @return this builder
	 */
	<T> Item.Settings customSetting(CustomItemSetting<T> setting, T value);
}
