package org.quiltmc.qsl.enchantment.api;

import org.jetbrains.annotations.Contract;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An interface for extending an {@link Item} with additional control over enchanting.
 */
public interface QuiltEnchantableItem {
	/**
	 * Determines whether the provided enchantment can be applied to this item.
	 * <p>
	 * This takes highest priority for applying enchantments.
	 * @param stack the stack containing this item
	 * @param enchantment the enchantment to apply to this item
	 * @return {@code true} if the enchantment can be applied, or {@code false} otherwise
	 */
	@Contract(pure = true)
	default boolean canEnchant(ItemStack stack, Enchantment enchantment) {
		return enchantment.isAcceptableItem(stack);
	}
}
