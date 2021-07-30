package org.quiltmc.qsl.enchantment.mixin;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EnchantedBookItem.class)
public class EnchantedBookItemMixin {

	@Shadow
	public static ItemStack forEnchantment(EnchantmentLevelEntry info) { throw new AssertionError("Mixin shadow error"); }

	@Inject(method = "appendStacks", at = @At("RETURN"))
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo callback) {
		if (group == ItemGroup.SEARCH) {
			Registry.ENCHANTMENT.stream().filter((enchantment) -> enchantment instanceof QuiltEnchantment).forEach((enchantment) -> {
				for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
					stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, level)));
				}
			});
		} else {
			Registry.ENCHANTMENT.stream()
					.filter((enchantment) -> enchantment instanceof QuiltEnchantment)
					.filter((enchantment) -> ((QuiltEnchantment) enchantment).isAcceptableItemGroup(group))
					.forEach((enchantment) -> stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel()))));
		}

	}
}
