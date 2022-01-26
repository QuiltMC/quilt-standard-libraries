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

package org.quiltmc.qsl.item.api.item.setting;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * A provider for the preferred {@link EquipmentSlot} of an item.
 * This can be used to give non-armor items, such as blocks,
 * an armor slot that they can go in.
 *
 * <p>{@link EquipmentSlotProvider} can be set with {@link QuiltItemSettings#equipmentSlot(EquipmentSlotProvider)}.
 *
 * <p>Note that items extending {@link net.minecraft.item.ArmorItem} don't need to use this
 * as there's {@link net.minecraft.item.ArmorItem#getSlotType()}.
 */
@FunctionalInterface
public interface EquipmentSlotProvider {
	/**
	 * Gets the preferred {@link EquipmentSlot} for an {@link ItemStack}.
	 *
	 * <p>If there is no preferred armor {@link EquipmentSlot} for the {@link ItemStack},
	 * {@link EquipmentSlot#MAINHAND} can be returned.
	 *
	 * @param stack The {@link ItemStack} to find the {@link EquipmentSlot} for
	 * @return The preferred {@link EquipmentSlot}
	 */
	EquipmentSlot getPreferredEquipmentSlot(ItemStack stack);
}
