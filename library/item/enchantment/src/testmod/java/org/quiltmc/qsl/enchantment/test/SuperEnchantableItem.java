package org.quiltmc.qsl.enchantment.test;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.enchantment.api.QuiltEnchantableItem;

public class SuperEnchantableItem extends Item implements QuiltEnchantableItem {
	public SuperEnchantableItem(Settings settings) {
		super(settings);
	}

	@Override
	public boolean canEnchant(ItemStack stack, Enchantment enchantment) {
		return true;
	}
}
