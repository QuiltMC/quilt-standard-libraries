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

package org.quiltmc.qsl.rendering.registration.impl.client;

import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.rendering.registration.api.client.DynamicItemRenderer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class DynamicItemRendererRegistryImpl {
	private static final Map<Item, DynamicItemRenderer> RENDERERS = new Object2ObjectOpenHashMap<>();

	private DynamicItemRendererRegistryImpl() {
	}

	public static void register(ItemConvertible itemConvertible, DynamicItemRenderer renderer) {
		Objects.requireNonNull(itemConvertible, "ItemConvertible cannot be null");
		Item item = itemConvertible.asItem();
		Objects.requireNonNull(item, "Item cannot be null");
		Objects.requireNonNull(renderer, "DynamicItemRenderer cannot be null");

		if (RENDERERS.putIfAbsent(item, renderer) != null) {
			throw new IllegalArgumentException("Item " + Registry.ITEM.getId(item) + " already has a DynamicItemRenderer!");
		}
	}

	@Nullable
	public static DynamicItemRenderer get(Item item) {
		return RENDERERS.get(item);
	}
}
