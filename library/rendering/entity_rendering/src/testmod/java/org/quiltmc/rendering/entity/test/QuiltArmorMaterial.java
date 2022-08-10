/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.rendering.entity.test;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;

public enum QuiltArmorMaterial implements ArmorMaterial {
	INSTANCE;

	private static final int[] BASE_DURABILITY = new int[] { 13, 15, 16, 11 };
	private static final int[] PROTECTION_AMOUNTS = new int[] { 3, 6, 8, 3 };
	private static final Ingredient REPAIR_INGREDIENT = Ingredient.ofTag(ItemTags.WOOL);

	@Override
	public int getDurability(EquipmentSlot slot) {
		return BASE_DURABILITY[slot.getEntitySlotId()] * 50;
	}

	@Override
	public int getProtectionAmount(EquipmentSlot slot) {
		return PROTECTION_AMOUNTS[slot.getEntitySlotId()];
	}

	@Override
	public int getEnchantability() {
		return 25;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_TURTLE;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return REPAIR_INGREDIENT;
	}

	@Override
	public String getName() {
		return "quilt";
	}

	@Override
	public float getToughness() {
		return 4.0F;
	}

	@Override
	public float getKnockbackResistance() {
		return 0.5F;
	}
}
