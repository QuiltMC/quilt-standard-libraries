/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.item.mixin;

import org.quiltmc.qsl.item.impl.AppliedItemSettingHooks;
import org.quiltmc.qsl.item.impl.QuiltItemInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;

import org.quiltmc.qsl.item.api.item.v1.CustomDamageHandler;
import org.quiltmc.qsl.item.api.item.v1.EquipmentSlotProvider;

@Mixin(Item.class)
abstract class ItemMixin implements AppliedItemSettingHooks {
	@Unique
	private EquipmentSlotProvider equipmentSlotProvider;

	@Unique
	private CustomDamageHandler customDamageHandler;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(Item.Settings settings, CallbackInfo info) {
		QuiltItemInternals.onBuild(settings, (Item) (Object) this);
	}

	@Override
	public EquipmentSlotProvider qsl$getEquipmentSlotProvider() {
		return equipmentSlotProvider;
	}

	@Override
	public void qsl$setEquipmentSlotProvider(EquipmentSlotProvider equipmentSlotProvider) {
		this.equipmentSlotProvider = equipmentSlotProvider;
	}

	@Override
	public CustomDamageHandler qsl$getCustomDamageHandler() {
		return customDamageHandler;
	}

	@Override
	public void qsl$setCustomDamageHandler(CustomDamageHandler handler) {
		this.customDamageHandler = handler;
	}
}
