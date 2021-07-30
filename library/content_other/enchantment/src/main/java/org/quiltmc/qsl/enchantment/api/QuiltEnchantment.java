package org.quiltmc.qsl.enchantment.api;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.collection.Weighted;
import org.quiltmc.qsl.enchantment.impl.ApplicationContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentContext;

import java.util.List;

public abstract class QuiltEnchantment extends Enchantment {
	public QuiltEnchantment() {
		super(Rarity.COMMON, null, null);
	}

	public abstract EquipmentSlot[] slots();

	/**
	 * Return an integer value that represents the weight of the enchantment given
	 * the current context. If you return 0 then your enchantment won't be added
	 * @param context
	 * @return
	 */
	public int weightFromEnchantmentContext(EnchantmentContext context) {
		if (context.getPower() >= this.getMinPower(context.getLevel()) && context.getPower() <= this.getMaxPower(context.getLevel())) {
			return 10; // Common
		}
		return 0; // Not added at all
	}

	/**
	 * Determines whether or not the given enchantment can be applied in the
	 * anvil under the current context
	 * @param context
	 * @return
	 */
	public abstract boolean isAcceptableApplicationContext(ApplicationContext context);

	public abstract boolean isAcceptableItemGroup(ItemGroup group);
}
