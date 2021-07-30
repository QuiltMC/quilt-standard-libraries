package org.quiltmc.qsl.enchantment.test;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemGroup;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.impl.ApplicationContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentContext;

public class ReapingEnchantment extends QuiltEnchantment {
	public ReapingEnchantment() {
		super();
	}

	@Override
	public EquipmentSlot[] slots() {
		return new EquipmentSlot[]{ EquipmentSlot.MAINHAND };
	}

	@Override
	public int weightFromEnchantmentContext(EnchantmentContext context) {
		return super.weightFromEnchantmentContext(context) > 0 && context.getStack().getItem() instanceof HoeItem ? 10 : 0;
	}

	@Override
	public boolean isAcceptableApplicationContext(ApplicationContext context) {
		return false;
	}

	@Override
	public boolean isAcceptableItemGroup(ItemGroup group) {
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
