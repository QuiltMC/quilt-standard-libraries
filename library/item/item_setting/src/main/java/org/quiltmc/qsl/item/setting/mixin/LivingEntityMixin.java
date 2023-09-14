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

package org.quiltmc.qsl.item.setting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Inject(
			method = "getPreferredEquipmentSlot",
			at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EquipmentSlot;MAINHAND:Lnet/minecraft/entity/EquipmentSlot;"),
			cancellable = true
	)
	private static void onGetPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
		var equipmentSlotProvider = CustomItemSettingImpl.EQUIPMENT_SLOT_PROVIDER.get(stack.getItem());

		if (equipmentSlotProvider != null) {
			cir.setReturnValue(equipmentSlotProvider.getPreferredEquipmentSlot(stack));
		}
	}
}
