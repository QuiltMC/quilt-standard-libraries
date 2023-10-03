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

	@Override
	public int getEnchantability() {
		return 25;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}
}
