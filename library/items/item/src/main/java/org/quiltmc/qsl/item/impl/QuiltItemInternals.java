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

package org.quiltmc.qsl.item.impl;

import java.util.WeakHashMap;

import net.minecraft.item.Item;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.api.item.v1.CustomDamageHandler;
import org.quiltmc.qsl.item.api.item.v1.EquipmentSlotProvider;

public final class QuiltItemInternals {
	private static final WeakHashMap<Item.Settings, ExtraData> extraData = new WeakHashMap<>();

	private QuiltItemInternals() {
	}

	public static ExtraData computeExtraData(Item.Settings settings) {
		return extraData.computeIfAbsent(settings, s -> new ExtraData());
	}

	public static void onBuild(Item.Settings settings, Item item) {
		ExtraData data = extraData.get(settings);

		if (data != null) {
			((AppliedItemSettingHooks) item).qsl$setEquipmentSlotProvider(data.equipmentSlotProvider);
			((AppliedItemSettingHooks) item).qsl$setCustomDamageHandler(data.customDamageHandler);
		}
	}

	public static final class ExtraData {
		private @Nullable EquipmentSlotProvider equipmentSlotProvider;
		private @Nullable CustomDamageHandler customDamageHandler;

		public void equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
			this.equipmentSlotProvider = equipmentSlotProvider;
		}

		public void customDamage(CustomDamageHandler handler) {
			this.customDamageHandler = handler;
		}
	}
}
