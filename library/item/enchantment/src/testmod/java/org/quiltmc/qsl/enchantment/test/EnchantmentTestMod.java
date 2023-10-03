/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.enchantment.test;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.enchantment.api.EnchantmentEvents;
import org.quiltmc.qsl.enchantment.api.context.PlayerUsingBlockEnchantingContext;

public class EnchantmentTestMod implements ModInitializer {
	public static final String MOD_ID = "quilt_enchantment_testmod";

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "super_enchantable"), new SuperEnchantableItem(new Item.Settings().maxDamage(32)));
		Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "reaping"), new ReapingEnchantment());
		Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "pervasive"), new PervasiveEnchantment());
		Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "merchant_greed"), new MerchantGreedEnchantment());

		EnchantmentEvents.MODIFY_POSSIBLE_ENCHANTMENTS.register((possibleEnchantments, context) -> {
			for (var iter = possibleEnchantments.iterator(); iter.hasNext();) {
				var entry = iter.next();
				if (entry.enchantment == Enchantments.SHARPNESS) {
					if (context instanceof PlayerUsingBlockEnchantingContext playerContext) {
						playerContext.getPlayer().sendMessage(Text.literal("Removed sharpness of level " + entry.level), true);
					}

					iter.remove();
				}
			}
		});

		EnchantmentEvents.ANVIL_APPLICATION.register((enchantment, context) -> enchantment != Enchantments.SILK_TOUCH);
	}
}
