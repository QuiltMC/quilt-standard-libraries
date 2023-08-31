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

package org.quiltmc.qsl.item.setting.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;

import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

/**
 * An interface describing the behavior for custom item settings.
 * This feature can be used by mods to add non-standard settings
 * to items in a way that is compatible with other mods that add
 * settings to items.
 * <p>
 * Values of this setting can be retrieved from an item using {@link CustomItemSetting#get(Item)},
 * and users that wish to expose a custom setting for use in other mods should do so by exposing
 * the CustomItemSetting instance.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create the setting instance. You can think of this as the "setting key".
 * public static final CustomItemSetting<String> CUSTOM_TOOLTIP = CustomItemSetting.create(() -> null);
 *
 * @Override
 * public void onInitializeClient(ModContainer mod) {
 *     ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
 *         // Gets the setting from the specified item.
 *         String tooltip = CUSTOM_TOOLTIP.get(stack.getItem());
 *
 *         if (tooltip != null) {
 *             lines.add(new LiteralText(tooltip));
 *         }
 *     }
 * }}</pre>
 * <p>
 * You should not implement this interface.
 * <p>
 * Use {@link CustomItemSetting#create(Supplier)} to retrieve an instance of QSL's implementation.
 *
 * @param <T> the type of the setting to be attached
 */
@ApiStatus.NonExtendable
public interface CustomItemSetting<T> {
	/**
	 * Returns the current value of this setting for the given {@link Item}.
	 *
	 * @param item the item
	 * @return the current setting if present, the default setting if not
	 */
	T get(Item item);

	/**
	 * Creates a new CustomItemSetting with the given default value.
	 *
	 * @param defaultValue the value all items that do not explicitly set this setting will have.
	 * @return a new CustomItemSetting
	 */
	static <T> CustomItemSetting<T> create(Supplier<T> defaultValue) {
		return new CustomItemSettingImpl<>(defaultValue);
	}

	/**
	 * Creates a new CustomItemSetting with the given default value.
	 *
	 * @param defaultValue the value all items that do not explicitly set this setting will have.
	 * @return a new CustomItemSetting
	 */
	static <T> CustomItemSetting<T> create(T defaultValue) {
		return create(() -> defaultValue);
	}
}
