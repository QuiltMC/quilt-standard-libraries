package org.quiltmc.qsl.enchantment.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.enchantment.api.QuiltEnchantment;
import org.quiltmc.qsl.enchantment.impl.EnchantmentContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;
import org.quiltmc.qsl.enchantment.mixinterface.MutableWeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Iterator;
import java.util.List;

@Mixin(value = EnchantmentHelper.class, priority = 1000)
public class EnchantmentHelperMixin {

	// This mixin prevents the whole "I can't get your mixin target" thingy
	@Redirect(method = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;iterator()Ljava/util/Iterator;"))
	private static Iterator<Enchantment> removeCustomEnchants(Registry<Enchantment> registry) {
		return registry.stream().filter((enchantment) -> !(enchantment instanceof QuiltEnchantment)).iterator();
	}

	@Inject(method = "getPossibleEntries", at = @At("RETURN"), cancellable = true)
	private static void handleCustomEnchants(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		List<EnchantmentLevelEntry> extraEntries = callback.getReturnValue();
		Registry.ENCHANTMENT.stream().filter((enchantment) -> enchantment instanceof QuiltEnchantment).forEach((enchantment) -> {
			for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
				EnchantmentContext context = EnchantmentGodClass.context.get().withLevel(level).withPower(power);
				int probability = ((QuiltEnchantment) enchantment).weightFromEnchantmentContext(context);
				if (probability > 0) {
					EnchantmentLevelEntry entry = new EnchantmentLevelEntry(enchantment, level);
					((MutableWeight) entry).setWeight(Weight.of(probability));
					extraEntries.add(entry);
				}
			}
		});
		callback.setReturnValue(extraEntries);
	}
}
