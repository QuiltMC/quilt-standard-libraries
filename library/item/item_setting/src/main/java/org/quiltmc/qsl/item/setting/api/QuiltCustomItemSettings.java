/*
 * Copyright 2022 QuiltMC
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

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

/**
 * A list of the {@link CustomItemSetting}s that are provided by Quilt
 */
public final class QuiltCustomItemSettings {
	private QuiltCustomItemSettings() {
	}

	/**
	 * The {@link CustomItemSetting} in charge of handing {@link EquipmentSlotProvider}s
	 */
	public static final CustomItemSetting<EquipmentSlotProvider> EQUIPMENT_SLOT_PROVIDER = CustomItemSettingImpl.EQUIPMENT_SLOT_PROVIDER;
	/**
	 * The {@link CustomItemSetting} in charge of handing {@link CustomDamageHandler}s
	 */
	public static final CustomItemSetting<CustomDamageHandler> CUSTOM_DAMAGE_HANDLER = CustomItemSettingImpl.CUSTOM_DAMAGE_HANDLER;
}
