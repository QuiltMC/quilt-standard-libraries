package org.quiltmc.qsl.enchantment.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantmentTestMod implements ModInitializer {
	@Override
	public void onInitialize() {
		Registry.register(Registry.ENCHANTMENT, new Identifier("reaping"), new ReapingEnchantment());
	}
}
