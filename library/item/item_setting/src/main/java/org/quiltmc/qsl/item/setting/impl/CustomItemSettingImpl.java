/*
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

package org.quiltmc.qsl.item.setting.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;

import org.quiltmc.qsl.item.setting.api.CustomDamageHandler;
import org.quiltmc.qsl.item.setting.api.CustomItemSetting;
import org.quiltmc.qsl.item.setting.api.EquipmentSlotProvider;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderLocation;
import org.quiltmc.qsl.item.setting.api.RecipeRemainderProvider;

@ApiStatus.Internal
public class CustomItemSettingImpl<T> implements CustomItemSetting<T> {
	public static final CustomItemSetting<EquipmentSlotProvider> EQUIPMENT_SLOT_PROVIDER = CustomItemSetting.create(() -> null);
	public static final CustomItemSetting<CustomDamageHandler> CUSTOM_DAMAGE_HANDLER = CustomItemSetting.create(() -> null);

	@SuppressWarnings("ConstantConditions")
	public static final CustomItemSetting<Map<RecipeRemainderLocation, RecipeRemainderProvider>> RECIPE_REMAINDER_PROVIDER = new CustomItemSettingImpl<>(HashMap::new) {
		@Override
		public void apply(Item.Settings settings, Item item) {
			if (item.hasRecipeRemainder()) {
				throw new IllegalArgumentException("Item cannot have a standard recipe remainder and a custom recipe remainder");
			}

			super.apply(settings, item);
		}
	};

	private static final Map<Item.Settings, Collection<CustomItemSettingImpl<?>>> CUSTOM_SETTINGS = new WeakHashMap<>();

	private final Map<Item.Settings, T> customSettings = new WeakHashMap<>();
	private final Map<Item, T> customItemSettings = new HashMap<>();
	private final Supplier<T> defaultValue;

	public CustomItemSettingImpl(Supplier<T> defaultValue) {
		Objects.requireNonNull(defaultValue);

		this.defaultValue = defaultValue;
	}

	@Override
	public T get(Item item) {
		Objects.requireNonNull(item);

		return this.customItemSettings.computeIfAbsent(item, i -> this.defaultValue.get());
	}

	public void set(Item.Settings settings, T value) {
		Objects.requireNonNull(settings);

		this.customSettings.put(settings, value);
		CUSTOM_SETTINGS.computeIfAbsent(settings, s -> new HashSet<>()).add(this);
	}

	public T get(Item.Settings settings) {
		Objects.requireNonNull(settings);

		CUSTOM_SETTINGS.computeIfAbsent(settings, s -> new HashSet<>()).add(this);
		return this.customSettings.computeIfAbsent(settings, _setting -> this.defaultValue.get());
	}

	public void apply(Item.Settings settings, Item item) {
		Objects.requireNonNull(settings);

		this.customItemSettings.put(item, this.customSettings.getOrDefault(settings, this.defaultValue.get()));
	}

	// Because item settings are reusable, it is possible that the same item settings object will be applied
	// to multiple items.
	public static void onBuild(Item.Settings settings, Item item) {
		for (CustomItemSettingImpl<?> setting : CUSTOM_SETTINGS.getOrDefault(settings, Collections.emptyList())) {
			setting.apply(settings, item);
		}
	}
}
