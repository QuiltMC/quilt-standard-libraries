/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.item.setting.api;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

/**
 * A provider for the {@link EquipmentSlot} that an item stack is able to be placed into.
 * This can be used to give non-armor items, such as blocks,
 * an armor slot that they can go in.
 * <p>
 * {@link EquipmentSlotProvider} can be set with {@link QuiltItemSettings#equipmentSlot(EquipmentSlotProvider)}.
 * <p>
 * Note that items extending {@link net.minecraft.item.ArmorItem} should
 * use {@link ArmorItem#getArmorSlot()} instead.
 */
@FunctionalInterface
public interface EquipmentSlotProvider {
	/**
	 * Gets the {@link EquipmentSlot} for an {@link ItemStack} to be placed into.
	 * <p>
	 * If there is no preferred armor {@link EquipmentSlot} for the {@link ItemStack},
	 * {@link EquipmentSlot#MAINHAND} can be returned.
	 *
	 * @param stack the {@link ItemStack} to find the {@link EquipmentSlot} for
	 * @return the preferred {@link EquipmentSlot}
	 */
	EquipmentSlot getPreferredEquipmentSlot(ItemStack stack);
}
